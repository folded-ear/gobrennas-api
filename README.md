# Cookbook!

I'm a cookbook! I'm a todo list! I'm a meal planning package! I'm awesome!

> Your _face_ is a cookbook!

## Build and Run

You'll need a recent-ish Maven and Node/NPM pair to build. Specific versions
may be stipulated at some point, but let's just say at least Maven 3 and
Node 10. Shut up about "old Node"; Maven 3 is the same age as Node itself, so
Node 10 is pretty damn new.

    cd client
    npm install
    npm run build
    cd ..
    mvn package

That'll leave you a nice self-running JAR file in the `target` directory,
which you'll want to launch with the following environment variables to point
it at a recent-ish Postgres (let's say 9.6 or newer) database of your choice:

    RDS_HOSTNAME=localhost \
    RDS_DB_NAME=foodinger \
    RDS_USERNAME=eng \
    RDS_PASSWORD=passwd \
    java -jar target/cookbook-0.0.1-SNAPSHOT.jar

Note that currently your Postgres *MUST* be running on `5432` (the standard
Postgres port).

Now just hit http://localhost:5000/ in your browser, and BAM.
