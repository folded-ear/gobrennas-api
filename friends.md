# Friends

Currently:

* "friend" is defined as "any other user".
* Recipes are currently visible to all "friends".
* Cook history is currently visible to all "friends".
* Plans require an explicit grant to a "friend" for them to have access (read, change, or admin).

# Conceptual Model

## friendships

A symmetric association between two users. Modeled as two one-directional associations (following / followed-by). Can be
broken by either user, which removes both one-directional associations.

This is the Facebook conceptual model described via the Twitter storage model.

## resources

Something owned by a user and which access control applies to. Owners _always_ have admin access to their resources.
Currently, there are two resource types:

* **A Recipe:** If you are a friend, you have read access.
* **A Plan:** Admins can grant view, modify, or admin access to friends.

These exemplify the two types of access control: "any friend" and via `AccessControlled` / `Acl`.

I forsee the library splitting into at least a couple different resource types, in particular a user-level "my library"
which individual recipes may be able to vary from. This means no more public recipes, which suggests the need for an "
any user" grant target. Also, a "what did I cook/what do I plan to cook" sort of timeline-ish, separate from the recipe
access. Perhaps "favorite recipes" along the same lines?

## grants

Some level of access a user has to a resource owned by a friend. Can be explicitly revoked at any time by a resource
admin **OR** the grantee. Grants are implicitly revoked if the friendship is broken.

Currently, there is no nesting of resources, so there's no need for override and/or revocation type grants. Yet.

Currently, there are no "any user" grants, other than by coincidence: every user is a "friend" of every other user. This
leads into groups...

Currently, there are not "my account" grants, which leads to delegation...

Currently, there are no anonymous grants, other than share links...

### groups

Not right now. If a user has so many friends they want to "friend groups" to ease ACL administration, they can write a
script. The API's publicly available on the internet, and GraphQL's explicitly designed to be auto-discoverable.

### delegation

Not right now. It's only tangentially related to friendship, and can be modeled as a grant on the `User` entity itself,
so wouldn't need special handling.

## share links

Effectively a permanent, non-revocable anonymous "read access" grant (without friendship), to any users and/or
non-users, via a secret _scoped to the resource being shared._ No rotation either.

This has some abuse-mitigation needs, TBD.

## invitations

"Sent" from a user to an anonymous recipient via a single-use secret. Once the anonymous recipient associates themselves
with a user account, the invitation becomes tied to that user. If accepted, it establishes friendship and optionally
creates a grant (if the invitation defined one). Multiple invitations from the same inviter, differing only in grant,
will be grouped together by default, so they can be accepted w/ a single action.

An invitation can be set as "multi-recipient" (with an upper bound) allowing many users to establish friendship (and
optionally, a grant) using the same secret. Each time a new user accesses the invitation, it gets cloned to a new
single-use invitation for that user, and the original's 'available' count decremented. The final use consumes the
original invitation itself. These "invitation families" can be identified by secret code, but are not explicitly
tracked.

New grants are created via accepting invitations; an admin's intent to issue a grant is expressed as "sending" an
invitation. Invitations to friends start out associated with the user, and are never "multi-use".

Invitations can be withdrawn by the sender, if not yet accepted. The recipient can decline an invitation, once
associated with a user account. A non-user recipient can only see an invitation, not act on it.

BFS doesn't actually _send_ invitations, only prepares them, so the inviting user must handle transmitting the
invitation via "something". BFS will show unanswered invitations to recipient users (once associated with the user) in
the app UI.

Invitations are only good for "a while" (TBD - a week?) so they will automatically die off without user action.

There are various additional abuse mitigation things needed here, TBD.

# Scenarios

## Brenna wants to invite Barney to help plan Our Week.

As owner, Brenna has admin on the plan, so she invites Barney w/ a write grant. Next time Barney signs in, he sees the
invite, hits accept, and can help.

## Nan wants to see what Brenna is cooking this week.

Nan asks Brenna to see her timeline. Brenna has admin, so she invites Nan w/ a read grant. Next time Nan signs in, she
sees the invite, hits accept, and can see Brenna's timeline.

## Brenna wants to seed Nan's plan for when Nan's in Portland next month.

1. Brenna calls Nan and walks through creating an invitation from Nan to Brenna over the phone.
1. Brenna knows Nan's Google password so just logs in as Nan directly and invites herself.
1. Brenna logs into the GCloud console and uses Cloud SQL Studio to manually create an invitation "from" Nan to herself
   to Nan's plan. Then logs into BFS, accepts the invite, and does "whatever".

What _should_ happen is Brenna can help Nan set up Brenna as a Nan-impersonator (aka delegate user). That will allow
Brenna to "be" Nan on BFS any ol' time. This is for the future.

## Barney wants to go look at Brenna's enchiladas recipe.

1. Brenna's library is public, so he just searches.
1. Brenna's library is private, so he asks her for access to either her library or that one recipe. Brenna invites
   Barney to whichever she thinks best. Once Barney accepts, he can see the recipe.

## Brenna wants to share her enchiladas recipe with Elliot.

1. Brenna's library is public, so she just sends Elliot a link.
1. Brenna's library is private, so she invites Elliot to either her library or that one recipe.
1. Brenna uses a share link to let Elliot (and whomever else) anonymously access that one recipe via a secret.

## Brenna wants to invite her brothers to see the vacation meal plan.

Brenna creates a "multi-recipient" invitation, bounded at `5` for her vacation mean plan w/ a read grant, and emails a
link. Once they create user accounts and accept, they can see the plan. Afterward, Brenna withdraws the invitation and
its remaining capacity. If the cap is hit due to fiendish behavior of a malicious actor (or typos, etc.), Brenna creates
another invitation and resends.

## Ben wants the ability to access all of Brenna's amazing recipes, without copying them to his library.

Ben texts Brenna for a friend request. Brenna immediately sends one, overwhelmed with delight. Once Ben accepts, her
recipes are visible in his library.

## Ben does not want to even be aware of Barney's terrible recipes.

Default state. Thank god.
