FROM golang:latest
RUN mkdir /app
ENV SERVER_PORT=8090 \
    SERVER_HOST=server \
    SOCKET_PATH=/tmp/agent.sock
ADD . /app/
RUN ls -l /app
WORKDIR /app
RUN go env -w GO111MODULE=auto
RUN go get -d -v ./...
RUN go build -o main .
CMD ["/app/main"]