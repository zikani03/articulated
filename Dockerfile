#
# MIT License
#
# Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

MAINTAINER Zikani Nyirenda Mwase <zikani.nmwase@ymail.com>
# This is a multi-stage build that uses two builder images to build the go service and the java service.
FROM golang:1.17-alpine as go-builder
RUN apk add --no-cache git
WORKDIR /go/quickprosener
COPY ./src/main/go/quickprosener .
RUN go generate -x -v
RUN go build -v -o /bin/quickprosener && chmod +x /bin/quickprosener

FROM maven:3.8.5-eclipse-temurin-17-alpine as java-builder
COPY . .
RUN mvn -Dmaven.test.skip=1 install
COPY ./target/articulated.jar /bin/articulated.jar

FROM openjdk:17-alpine
EXPOSE 4000
EXPOSE 4567
COPY --from=go-builder /bin/quickprosener /quickprosener
COPY --from=java-builder /src/target/articulated.jar /articulated.jar
# COPY ./target/articulated.jar /articulated.jar
COPY ./docker-entrypoint.sh /docker-entrypoint.sh 
RUN chmod +x /docker-entrypoint.sh
CMD [ "/docker-entrypoint.sh"]