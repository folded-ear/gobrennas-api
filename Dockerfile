# build environment
FROM eclipse-temurin:17-alpine AS build
WORKDIR /app
COPY . ./
RUN ./mvnw --batch-mode -Dtest-containers=disabled verify
RUN java -Djarmode=tools \
    -jar target/cookbook-1.0.0-SNAPSHOT.jar \
    extract \
    --layers \
    --destination layers

# minify environment
FROM node:20 AS minify
WORKDIR /app
COPY --from=build /app/layers/application/ ./
RUN apt-get update  \
    && apt-get install -y unzip zip
RUN mkdir public/ \
    && unzip cookbook-1.0.0-SNAPSHOT.jar public/import_bookmarklet.js \
    && mv public/import_bookmarklet.js public/import_bookmarklet.orig.js
RUN npx esbuild \
        public/import_bookmarklet.orig.js \
        --minify \
        --outfile=public/import_bookmarklet.js
RUN zip cookbook-1.0.0-SNAPSHOT.jar public/import_bookmarklet.js \
    && unzip -l cookbook-1.0.0-SNAPSHOT.jar public/import_bookmarklet.js
RUN ls -l public/ \
    && rm -rf public
RUN ls -l

# server environment
FROM eclipse-temurin:17-alpine
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
COPY --from=minify /app/ ./
ENTRYPOINT ["java", "-jar", "cookbook-1.0.0-SNAPSHOT.jar"]
