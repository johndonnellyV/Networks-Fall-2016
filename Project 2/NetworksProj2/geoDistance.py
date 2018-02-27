import httplib
from math import fabs, sin, cos, sqrt, asin, radians
import json

#jed216

#gets the coordinates of the hostname desired and yourself
def getCoord(hostName):

    geoDataTarget = httplib.HTTPConnection('freegeoip.net')
    geoDataSelf = httplib.HTTPConnection('freegeoip.net')

    geoDataTarget.request('GET', '/json/%s' % (hostName))
    geoDataSelf.request('GET', '/json/')
    # array of data on self for lat and long

    targetLocData =  json.loads(geoDataTarget.getresponse().read())
    ownLocData =  json.loads(geoDataSelf.getresponse().read())

    geoDataTarget.close()
    geoDataSelf.close()

    return round(float(targetLocData['latitude']),5), round(float(targetLocData['longitude']),5), round(float(ownLocData['latitude']),5), round(float(ownLocData['longitude']),5)


# This method was based on one from stackoverflow
# uses the "haversine formula to calculate distance around the planet
def distanceCalc(longitude1, latitude1, longitude2, latitude2):

    longitude1, latitude1, longitude2, latitude2 = map(radians, [longitude1, latitude1, longitude2, latitude2])

    deltaLong = fabs(longitude2 - longitude1)
    deltaLat = fabs(latitude2 - latitude1)
    a = sin(deltaLat/2)**2 + cos(latitude1) * cos(latitude2) * sin(deltaLong / 2) ** 2
    c = 2 * asin(sqrt(a))

    km = 6371 * c #radius of Earth according to google

    return int(km)

# actually runs the geographic test
def runTest(hostName):

    longitude1, latitude1, longitude2, latitude2 = getCoord(hostName)

    distance = distanceCalc(longitude1, latitude1, longitude2, latitude2)
    print 'Geographic Distance from: %s (%s km)\n' % (hostName, distance)

#main method
if __name__ == '__main__':
    targetFile = open('targets.txt', 'r')
    # loop through each line of text
    for line in targetFile:
        runTest(line.split()[0])
    targetFile.close()