FROM openjdk:15-alpine
MAINTAINER Zikani Nyirenda Mwase <zikani.nmwase@ymail.com>
COPY ./target/articulated.jar /articulated.jar
EXPOSE 4567
CMD [ "java", "-Dlogging.fileName=/logs/api.log", "--enable-preview", "-jar", "/articulated.jar" ]
