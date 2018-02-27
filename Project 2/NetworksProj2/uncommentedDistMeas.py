import socket
import time

#jed126

#class var, hop limit from specs
hopLimit = 32

# This method was found on stackoverflow
# just makes a string out of an IP
def makeString(ip):
    return str(ord(ip[0])) + "." + str(ord(ip[1])) + "." + str(ord(ip[2])) + "." + str(ord(ip[3]))

# This method sends out a UDP packet to a specific host and returns
# the ICMP response that is given and the rtt
def getPing(hostName, timeToLive=32, portNumber=38576):

    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    timeUntilTimeout = 2
    # creates the sockets to send and receive

    outGoingSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
    incomingSocket= socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)

    currentIPAddress = None
    currentName = None
    package = 'Clevo P650RS-G' # bet you don't know what that is without google ;)
    targetAddress = socket.gethostbyname(hostName)

    # timeout of 2 seconds as asked
    incomingSocket.settimeout(timeUntilTimeout)
    incomingSocket.bind(('', portNumber))

    outGoingSocket.setsockopt(socket.SOL_IP, socket.IP_TTL, timeToLive)

    # sends the packet out
    outGoingSocket.sendto(package, (hostName, portNumber))

    initialTime = time.time()
    finalTime = time.time() + timeUntilTimeout #final - initial for rtt

    try:
        # reads the hopefully successful response
        receivedDataString, currentIPAddress = incomingSocket.recvfrom(2048)
        currentIPAddress = currentIPAddress[0]
        finalTime = time.time()

    except socket.error:
        #if error here just close
            return '', -1

    finally:

        outGoingSocket.close()
        incomingSocket.close()

    timeDif = round((finalTime - initialTime)*1000, 2)

    #validate the data we got
    # To validate the data I used 3 check, the type byte should be 3, and the
    # destination a and source IPs must match


    typeByte = ord(receivedDataString[20])
    destinationBytes = makeString(receivedDataString[44:48])
    sourceByte = makeString(receivedDataString[12:16])

    # do the checks

    if destinationBytes != targetAddress:
        print "Destination IP is incorrect for %s." % (targetAddress)
        return currentIPAddress, timeDif
    if sourceByte != targetAddress:
        print "Source IP is incorrect for %s." % (targetAddress)
        return currentIPAddress, timeDif
    if typeByte != 3:
        print "Packet was not 3 from %s." % (targetAddress)
        return currentIPAddress, timeDif

    return currentIPAddress, timeDif

# counts the total hops by using the header data received
def countTotalHops(hostName):

    min = 0
    max = hopLimit
    timeToLive = 0

    while min < max:
        #quick duplication prevention don't want the same thing to run
        if timeToLive == (max + min)/2:
            break
        #start of finding the ttl for real
        else:
            timeToLive = (max + min)/2

        target, responseTime = getPing(hostName, timeToLive) # try reaching host with ttl number of hops

        #if it takes longer than 2000 seconds timeout
        if responseTime > 2000:
            break
        #if there is no IP the ttl surpassed the list limit
        if target == None:
            max = timeToLive
            #if the hostname is found it MUST be last
            #This is because a ttl that's too high would timeout, and too
            #low wouldn't have the corrent host name yet
        elif target.find(hostName) > 0:
            return timeToLive;

        # If it hits here the ttl was too low, the host wasn't hit yet
        else:
            min = timeToLive

    return min
# finds the rtt of an ip using the time to live (requires total hops)
def roundTripTimeToIP(hostName, timeToLive):

    currentIPAddress, roundTripTime = getPing(hostName, timeToLive)
    return round(roundTripTime,1)


# runs the tests and reports rtt and number of hops to the console
def runTests(hostName):

    targetAddress = socket.gethostbyname(hostName)
    totalHops = countTotalHops(targetAddress)
    time = roundTripTimeToIP(targetAddress, totalHops)

    print 'Round Trip Time to: %s (%s ms)' % (hostName, time)
    print 'Number of Hops to: %s (%s)' % (hostName, totalHops)


# main method
if __name__ == '__main__':
    targetFile = open('targets.txt', 'r')
    # loop through each line of text
    for line in targetFile:
        runTests(line.split()[0])
    targetFile.close()