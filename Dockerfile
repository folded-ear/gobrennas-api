# build environment
FROM eclipse-temurin:17-alpine as build
WORKDIR /app
COPY . ./
RUN ./mvnw --batch-mode verify
RUN java -Djarmode=layertools -jar target/cookbook-1.0.0-SNAPSHOT.jar extract

# server environment
FROM eclipse-temurin:17-alpine
COPY --from=build /app/dependencies/ ./
COPY --from=build /app/spring-boot-loader/ ./
COPY --from=build /app/snapshot-dependencies/ ./
COPY --from=build /app/application/ ./
ENV PORT=80 \
    HOST=0.0.0.0
EXPOSE $PORT
ENTRYPOINT ["java", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "com.brennaswitzer.cookbook.CookbookApplication"]
