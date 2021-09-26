# Foodinger!

[![CI/CD](https://github.com/folded-ear/foodinger/actions/workflows/ci-cd.yaml/badge.svg)](https://github.com/folded-ear/foodinger/actions/workflows/ci-cd.yaml)

I'm a cookbook! I'm a todo list! I'm a meal planning package! I'm awesome!

> Your _face_ is a cookbook!

## Build and Test

You'll need a recent-ish Maven and Node/NPM pair to build. Specific versions may
be stipulated at some point, but let's just say at least Maven 3 and Node 10.
Shush about "old Node"; Maven 3 is the same age as _Node itself_, so Node 10 is
pretty damn new. Take your ADHD pills and let's keep going.

The easiest way is to install `nvm` (see https://github.com/nvm-sh/nvm ) and use
the included `mvnw` script:

    nvm use # run `nvm install` if it says you need to
    cd client
    npm install
    npm run build
    cd ..
    ./mvnw package

Now you'll have a nice self-running JAR file in the `target` directory. Which
we'll immediately forget about, because it's for deployment, not development.
However, you _did_ just run the full regression suite (as would `./mvnw test`)!

## Run (For Development)

You'll need a recent-ish Postgres (let's say 9.6 or newer) database to run
against. Note that your Postgres *MUST* be running on `5432` (the standard
Postgres port). If you're using a decent OS - or Docker Desktop; 🙄 - you'll
have `docker` available, which is a great choice:

    docker run -d --name pg -p 5432:5432 -e POSTGRES_PASSWORD=passwd postgres:9.6

If you already have existing PG infrastructure, create a new database (unless
your `postgres` database remains pristine, and you want to use it).

To run the app, you'll need two terminals, one for the server:

    RDS_HOSTNAME=localhost \
    RDS_DB_NAME=postgres \
    RDS_USERNAME=postgres \
    RDS_PASSWORD=postgres \
    ./mvnw spring-boot:run

and one for the client:

    cd client
    npm start

The latter should have opened http://localhost:3001/ in your default browser,
but if not, hit that link manually. BAM.

## Run (For Production)

That self-running JAR from the "Build and Test" section is perfect! Except you
also need Google Auth secrets, DNS configuration, the right hostnames, the
`package.json` config, and a bunch of mess. All of which is normal "host this
thing" boilerplate and has nothing to do with Foodinger. So figure it out. :)
