# Brenna's Food Software API

Do you use food? Do you use software? Brenna's Food Software is for you!

> Your _face_ is ~~a cookbook~~ food software!

## Build and Test

You'll need Java 17 to build. A more specific version may be stipulated at some
point:

    ./mvnw package

Now you'll have a nice self-running JAR file in the `target` directory. Which
we'll immediately forget about, because it's for deployment, not development.
However, you _did_ just run the full regression suite!

## Run (For Development)

You'll need a Postgres 15 database to run against. If you're using a decent OS -
or Mac w/ Docker Desktop; 🙄 - you'll have `docker` available, which is a great
choice:

    docker run -d --name pg -p 5432:5432 -e POSTGRES_PASSWORD=passwd postgres:15

If you already have existing PG infrastructure, create a new database (unless
you want to use your - still pristine - `postgres` database).

> ### Upgrading from PG 10
>
> First, dump your current database:
>
>     pg_dump -h localhost -d postgres -U postgres --no-owner > gobrennas.sql
>
> Stop your Postgres 10 container/server, start up Postgres 15, and restore:
>
>     psql -h localhost -U postgres -v ON_ERROR_STOP=1 < gobrennas.sql

To run:

    DB_HOST=localhost \
    DB_NAME=postgres \
    DB_USER=postgres \
    DB_PASS=passwd \
    ./mvnw spring-boot:run

You might create a `src/main/resources/application-default.yml` with your
settings (look to the other `application*.yml` in that directory for
inspiration) instead of using environment variables. They're equivalent.

Open http://localhost:5000/ in your browser and ... see a 404 error. You have an
API without a client, which isn't very useful. Check out
https://github.com/folded-ear/gobrennas-client, a local instance of which is
where the error's link wants to bring you.

> ***NB:*** By default, you're using Folded Ear's development OAuth 2.0 client
> ID. Its keys are hard coded in `application.yml`, and it's locked to
> `localhost`. We deemed this "secure enough" to lubricate the developer
> experience, particularly during bootstrap. We promise not to use demo accounts
> for anything nefarious. You can decide whether to trust us or not. If "or
> not", create an app and configure your id and secret in
> `application-default.yml` to override the defaults.

## Run (For Production)

That self-running JAR from the "Build and Test" section is perfect! Except you
also need Google Auth and AWS secrets, DNS configuration, the right hostnames, a
matched client, and a bunch of mess. All of which is normal "host this thing"
boilerplate. So figure it out. :) You may find `application-production.yml`
useful, illustrating one way to parameterize things.

You can hit https://api.gobrennas.com/ to see it in action.
