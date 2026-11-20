# build environment
FROM eclipse-temurin:17.0.21.1_2-jdk-alpine AS build
WORKDIR /app
COPY . ./
RUN apk add --no-cache esbuild \
    && ./mvnw --batch-mode -Dtest-containers=disabled verify \
    && java -Djarmode=tools \
        -jar target/cookbook-1.0.0-SNAPSHOT.jar \
        extract \
        --layers \
        --destination layers
WORKDIR /app/layers/application
RUN jar xf cookbook-1.0.0-SNAPSHOT.jar public/import_bookmarklet.js \
    && mv public/import_bookmarklet.js public/import_bookmarklet.orig.js \
    && esbuild \
        public/import_bookmarklet.orig.js \
        --minify \
        --outfile=public/import_bookmarklet.js \
    && jar uf cookbook-1.0.0-SNAPSHOT.jar public/import_bookmarklet.js \
    && ls -l public/ \
    && rm -rf public

# server environment
FROM eclipse-temurin:17.0.21.1_2-jdk-alpine
ENV PORT=80 \
    HOST=0.0.0.0
EXPOSE $PORT
WORKDIR /app
COPY --from=build /app/layers/dependencies/ ./
COPY --from=build /app/layers/spring-boot-loader/ ./
COPY --from=build /app/layers/snapshot-dependencies/ ./
# A no-op COPY followed by another COPY loses a layer, so add
# a "garbage" RUN, since we generally don't have snapshot deps.
# See https://github.com/moby/moby/issues/37965
RUN true
COPY --from=build /app/layers/application/ ./
ENTRYPOINT ["java", "-jar", "cookbook-1.0.0-SNAPSHOT.jar"]
