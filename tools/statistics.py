import matplotlib.pyplot as plt
import pandas as pd
import pyshark
import getopt
import sys

packets_sent = [0, 0, 0]
packets_retransmitted = [0, 0, 0]
bytes_sent = [0, 0, 0]
bytes_retransmitted = [0, 0, 0]
index = ['UDP', 'TCP', 'SCTP']

def create_plot(dataframe, ax):
    dataframe.plot(kind='barh', ax=ax, width=0.8)
    for i in range(len(dataframe.values)):
        item_values = dataframe.values[i]
        for j in range(len(item_values)):
            item_value = item_values[j]
            ax.text(item_value / 2, i+j*0.4-0.3, item_value, color='white')

# Get CLI Options
argv = sys.argv[1:]
port = 667
try:
    opts, args = getopt.getopt(argv, 'f:p', ['file=', 'port='])
    for o, a in opts:
        if o in ('-f', '--file'):
            pcap_file = a
        if o in ('-p', '--port'):
            port = a
except getopt.GetoptError:
    print('Invalid syntax!')
    sys.exit(2)

protocol_filter = '(udp.port=={port}||tcp.port=={port}||sctp.port=={port})'.format(port=port)
protocol_retransmission_filter = '(tcp.analysis.retransmission||sctp.retransmission)'

capture = pyshark.FileCapture(pcap_file, use_json=True, display_filter=protocol_filter)
for packet in capture:
    if 'UDP' in str(packet.layers[2]):
        packets_sent[0] += 1
        bytes_sent[0] += packet.length
    if 'TCP' in str(packet.layers[2]):
        packets_sent[1] += 1
        bytes_sent[1] += packet.length
    if 'SCTP' in str(packet.layers[2]):
        packets_sent[2] += 1
        bytes_sent[2] += packet.length

capture = pyshark.FileCapture(pcap_file, use_json=True, display_filter=protocol_filter + '&&' + protocol_retransmission_filter)
for packet in capture:
    if 'TCP' in str(packet.layers[2]):
        packets_retransmitted[1] += 1
        bytes_retransmitted[1] += packet.length
    if 'SCTP' in str(packet.layers[2]):
        packets_retransmitted[2] += 1
        bytes_retransmitted[2] += packet.length

packets_dataframe = pd.DataFrame({'Packets sent': packets_sent, 'Packets retransmitted': packets_retransmitted}, index=index)
bytes_dataframe = pd.DataFrame({'Bytes sent': bytes_sent, 'Bytes retransmitted': bytes_retransmitted}, index=index)

fig, [ax_packets, ax_bytes] = plt.subplots(2)
create_plot(packets_dataframe, ax_packets)
create_plot(bytes_dataframe, ax_bytes)

fig.canvas.manager.set_window_title('Distributed Sniffer Statistics')
plt.show()