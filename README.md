# Java Distributed Sniffer
```console
Usage: distributed-sniffer [-hnV] [-c=<captureCommand>] [-o=<output>]
                           [-p=<serverPort>] [-P=<protocolName>] [-s=<host>]
                           [-S=<hostFile>]
  -c, --command=<captureCommand>
                            Command to perform packet capture (default=tcpdump)
  -h, --help                Show this help message and exit.
  -n, --no-serve            Does not act as a server visible to other hosts
  -o, --output=<output>     Output file. Standard input is used if ouput is "-"
  -p, --port=<serverPort>   Server port (default=667)
  -P, --protocol=<protocolName>
                            protocol used for sending messages between client
                              and server ([UDP], TCP, SCTP)
  -s, --host=<host>         Host to sniff
  -S, --host-file=<hostFile>
                            File with hosts to sniff
  -V, --version             Print version information and exit.
```
## Main classes
![Class diagram](https://i.imgur.com/RG09Dtf.png)

## UDP Communication flow
The UDP protocol is not connection oriented, so it was necessary to create a messaging system to identify when a new client connects or disconnects from the broadcast:
![Flow diagram](https://i.imgur.com/yTjoTbD.png)

## TCP/SCTP Communication
The client/server communication implemented for TCP and SCTP protocols is similar. The client's addHost() method opens a connection in a new thread and listens for new messages coming from the server. The server will remove clients that are no longer connected.

## Example of communication scenario between instances
![Example](https://i.imgur.com/NdL914m.png)

## Using Docker to test
Starting a server only
```console
docker run --name sniffer-server --rm tcpassos/distributed-sniffer
```
Starting a client that will listen to a server
```console
docker run --name sniffer-listener --rm tcpassos/distributed-sniffer --no-serve --host=<<server address>>
```
Or starting a client that will listen to multiple servers (the hosts file has the IP address of each server separated by lines)
```console
docker run --rm \
    --name sniffer-listener \
    -v hosts_file_directory:/usr/src/files \
    tcpassos/distributed-sniffer \
    --host-file=/usr/src/files/hosts.txt
```
Simulating requests:
```console
docker exec sniffer-server /usr/bin/curl google.com
```
We can analyze packet traffic between containers with tcpdump running in another container:
```console
docker run --rm --net=host -v "$PWD":/tcpdump kaazing/tcpdump
```
## Viewing statistics for each protocol
The script available at /tools/statistics.py can graph statistics for each implemented protocol
```console
python3 statistics.py --file example.pcap --port 667
```
![Statistics](https://i.imgur.com/RT0pXrs.png)
## Video Example
[![Example](https://img.youtube.com/vi/pFsOG_oaS_4/0.jpg)](https://www.youtube.com/watch?v=pFsOG_oaS_4)