FROM golang:1.19.2-alpine AS builder
COPY . /src
WORKDIR /src
RUN CGO_ENABLED=0 go build -o /invoice cmd/invoice/main.go

FROM gcr.io/distroless/static-debian11
COPY --from=builder /invoice /
ENTRYPOINT ["/invoice"]
