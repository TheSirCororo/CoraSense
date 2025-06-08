FROM gradle:jdk17 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle.* settings.gradle.* gradle.properties /home/gradle/app/
COPY server/build.gradle.* /home/gradle/app/server
COPY telegram-bot/build.gradle.* /home/gradle/app/telegram-bot
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle clean -i --stacktrace

FROM gradle:jdk17 AS build
ARG MODULE
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN echo ${MODULE} && gradle ${MODULE}:build -DskipTests=true --no-daemon

FROM openjdk:17-jdk AS runtime
ARG MODULE
ENV MODULE=$MODULE
RUN mkdir /app
COPY --from=build /home/gradle/src/$MODULE/build/libs/*-all.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]