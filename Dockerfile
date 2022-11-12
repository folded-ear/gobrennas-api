# build environment
FROM eclipse-temurin:8-alpine as build
WORKDIR /app
COPY . ./
RUN ./mvnw --batch-mode verify

# server environment
FROM eclipse-temurin:8-alpine
COPY --from=build /app/target/cookbook-0.0.1-SNAPSHOT.jar .
ENV PORT=80 \
    HOST=0.0.0.0
EXPOSE $PORT
CMD java -jar cookbook-0.0.1-SNAPSHOT.jar
