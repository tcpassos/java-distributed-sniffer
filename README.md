# Java Distributed Sniffer
```console
Usage: Distributed Sniffer [-hnV] [-c=<captureCommand>] [-o=<output>]
                           [-p=<serverPort>] [-s=<host>] [-S=<hostFile>]
  -c, --command=<captureCommand>
                            Command to perform packet capture (default=tcpdump)
  -h, --help                Show this help message and exit.
  -n, --no-serve            Does not act as a server visible to other hosts
  -o, --output=<output>     Output file. Standard input is used if ouput is "-"
  -p, --port=<serverPort>   Server port (default=667)
  -s, --host=<host>         Host to sniff
  -S, --host-file=<hostFile>
                            File with hosts to sniff
  -V, --version             Print version information and exit.
```
## Main classes
![Class diagram](https://i.imgur.com/tbbO3J0_d.webp?maxwidth=760&fidelity=grand)

## Communication flow
![Flow diagram](https://i.imgur.com/yTjoTbD.png)

## Example of communication scenario between instances
![Example](https://i.imgur.com/NdL914m.png)

## Using Docker to test
```console
docker run --name <name> --rm tcpassos/distributed-sniffer -h
```
Simulating requests:
```console
docker exec <name> /usr/bin/curl google.com
```
We can analyze packet traffic between containers with tcpdump running in another container:
```console
docker run --rm --net=host -v "$PWD":/tcpdump kaazing/tcpdump
```
