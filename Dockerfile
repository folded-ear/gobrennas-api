# build environment
FROM eclipse-temurin:17-alpine AS build
WORKDIR /app
COPY . ./
RUN ./mvnw --batch-mode -Dtest-containers=disabled verify \
    && java -Djarmode=layertools -jar target/cookbook-1.0.0-SNAPSHOT.jar extract

# minify environment
FROM node:20 AS minify
WORKDIR /app
COPY --from=build /app/application/BOOT-INF/classes/public/import_bookmarklet.js ./
RUN mv import_bookmarklet.js import_bookmarklet.orig.js \
    && npx esbuild \
        import_bookmarklet.orig.js \
        --minify \
        --outfile=import_bookmarklet.js

# server environment
FROM eclipse-temurin:17-alpine
COPY --from=build /app/dependencies/ ./
COPY --from=build /app/spring-boot-loader/ ./
COPY --from=build /app/snapshot-dependencies/ ./
# A no-op COPY followed by another COPY loses a layer, so add
# a "garbage" RUN, since we generally don't have snapshot deps.
# See https://github.com/moby/moby/issues/37965
RUN true
COPY --from=build /app/application/ ./
COPY --from=minify /app/import_bookmarklet.js ./BOOT-INF/classes/public/
# Can't use a directory of loose classes w/ CDS, so make a JAR.
RUN jar --create --file cookbook.jar -C BOOT-INF/classes . \
    && java -cp 'cookbook.jar:BOOT-INF/lib/*' \
        -XX:ArchiveClassesAtExit=cookbook.jsa \
        -Dspring.context.exit=onRefresh \
        # There's no database, so turn off some stuff, though it'll reduce the
        # classes loaded, and thus the benefits of CDS...
        # Hibernate will WARN w/ a ghastly stack trace, but it gracefully falls
        # back to the explicit config.
        #   HHH000342: Could not obtain connection to query metadata
        -Dspring.jpa.hibernate.ddl-auto=none \
        # This "disables" HikariCP, in a roundabout way.
        -Dspring.datasource.type=org.springframework.jdbc.datasource.SimpleDriverDataSource \
        # Liquibase can be done directly.
        -Dspring.liquibase.enabled=false \
        com.brennaswitzer.cookbook.CookbookApplication
ENV PORT=80 \
    HOST=0.0.0.0
EXPOSE $PORT
ENTRYPOINT ["java", "-cp", "cookbook.jar:BOOT-INF/lib/*", \
    "-XX:SharedArchiveFile=cookbook.jsa", \
    "com.brennaswitzer.cookbook.CookbookApplication"]
