// Code generated by protoc-gen-go-grpc. DO NOT EDIT.

package client

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.32.0 or later.
const _ = grpc.SupportPackageIsVersion7

// EchoClient is the client API for Echo service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type EchoClient interface {
	Send(ctx context.Context, in *Message, opts ...grpc.CallOption) (*Message, error)
}

type echoClient struct {
	cc grpc.ClientConnInterface
}

func NewEchoClient(cc grpc.ClientConnInterface) EchoClient {
	return &echoClient{cc}
}

func (c *echoClient) Send(ctx context.Context, in *Message, opts ...grpc.CallOption) (*Message, error) {
	out := new(Message)
	err := c.cc.Invoke(ctx, "/echo.Echo/Send", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// EchoServer is the server API for Echo service.
// All implementations must embed UnimplementedEchoServer
// for forward compatibility
type EchoServer interface {
	Send(context.Context, *Message) (*Message, error)
	mustEmbedUnimplementedEchoServer()
}

// UnimplementedEchoServer must be embedded to have forward compatible implementations.
type UnimplementedEchoServer struct {
}

func (UnimplementedEchoServer) Send(context.Context, *Message) (*Message, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Send not implemented")
}
func (UnimplementedEchoServer) mustEmbedUnimplementedEchoServer() {}

// UnsafeEchoServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to EchoServer will
// result in compilation errors.
type UnsafeEchoServer interface {
	mustEmbedUnimplementedEchoServer()
}

func RegisterEchoServer(s grpc.ServiceRegistrar, srv EchoServer) {
	s.RegisterService(&Echo_ServiceDesc, srv)
}

func _Echo_Send_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(Message)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(EchoServer).Send(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/echo.Echo/Send",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(EchoServer).Send(ctx, req.(*Message))
	}
	return interceptor(ctx, in, info, handler)
}

// Echo_ServiceDesc is the grpc.ServiceDesc for Echo service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var Echo_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "echo.Echo",
	HandlerType: (*EchoServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "Send",
			Handler:    _Echo_Send_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "src/main/proto/echo.proto",
}
