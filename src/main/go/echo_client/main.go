package main

import (
	"context"
	"crypto/tls"
	"google.golang.org/grpc/credentials"
	"log"
	"net"
	"os"
	"time"

	pb "./client"
	"google.golang.org/grpc"
)

func main() {
	path, exists := os.LookupEnv("SOCKET_PATH")
	if !exists {
		path = "/tmp/agent.sock"
	}
	host, exists := os.LookupEnv("SERVER_HOST")
	if !exists {
		host = "server"
	}
	port, exists := os.LookupEnv("SERVER_PORT")
	if !exists {
		port = "8090"
	}
	// Set up a connection to the server.
	config := &tls.Config{
		InsecureSkipVerify: true,
	}
	conn, err := grpc.Dial(host + ":" + port, grpc.WithTransportCredentials(credentials.NewTLS(config)), grpc.WithBlock())
	if err != nil {
		log.Fatalf("fail to connect to server: %v", err)
	} else {
		log.Printf("connected to server: %s", host + ":" + port)
	}
	defer conn.Close()
	serverStub := pb.NewEchoClient(conn)

	connAgent, err := grpc.Dial(path, grpc.WithInsecure(), grpc.WithDialer(func(addr string, timeout time.Duration) (net.Conn, error) {
		return net.DialTimeout("unix", addr, timeout)
	}))
	if err != nil {
		log.Fatalf("fail to connect to agent: %v", err)
	} else {
		log.Printf("connected to agent: %s", path)
	}
	defer connAgent.Close()
	agentStub := pb.NewEchoClient(connAgent)

	for i := 1; i > 0; i++ {
		_, err := serverStub.Send(context.Background(), &pb.Message{Source: "client-go-server", Timestamp: time.Now().UnixNano(), Message: "hello", Id: int64(i)})
		if err != nil {
			log.Fatalf("could not exchange with server: %v", err)
		}
		//log.Printf("Echo server: %d", r.GetId())

		_, err = agentStub.Send(context.Background(), &pb.Message{Source: "client-go-agent", Timestamp: time.Now().UnixNano(), Message: "hello", Id: int64(i)})
		if err != nil {
			log.Fatalf("could not exchange with agent: %v", err)
		}
		//log.Printf("Echo agent: %d", r.GetId())
	}

}