# build environment
FROM eclipse-temurin:17-alpine AS build
WORKDIR /app
COPY . ./
RUN ./mvnw --batch-mode -Dtest-containers=disabled verify
RUN java -Djarmode=layertools -jar target/cookbook-1.0.0-SNAPSHOT.jar extract

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
ENV PORT=80 \
    HOST=0.0.0.0
EXPOSE $PORT
ENTRYPOINT ["java", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "com.brennaswitzer.cookbook.CookbookApplication"]
