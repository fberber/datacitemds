import requests, sys

#endpoint = 'https://mds.datacite.org/metadata'
endpoint = 'https://mds.test.datacite.org/metadata'

if (len(sys.argv) < 4):
    raise Exception('Please provide username, password and doi')

username, password, doi = sys.argv[1:]

response = requests.get(endpoint + '/' + doi,
                        auth = (username, password),
                        headers = {'Accept':'application/xml'})

if (response.status_code != 200):
    print str(response.status_code) + " " + response.text
else:
    print response.text
