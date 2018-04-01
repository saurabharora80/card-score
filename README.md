# card-score
API to find credit card offers for an applicant

[![Build Status](https://travis-ci.org/saurabharora80/card-score.svg?branch=master)](https://travis-ci.org/saurabharora80/card-score)

## Running tests

All the tests can be executed by running ```sbt test```

## Application StartUp

Application can be started by executing 

a) start.sh script (i.e ```./start.sh```) on a random port 

b) ```sbt run``` on fixed port 8080    

#### Accessing API from command line

1. Install [httpie](https://httpie.org/)
2. Execute ```sbt run```. This is should start the server on http://localhost:8080/ 
2. Execute ```http POST localhost:8080/creditcards firstname=saurabh lastname=arora dob=1980/10/10 credit-score:=500 employment-status=FULL_TIME salary:=1000```
3. You should receive the following response 
```
HTTP/1.1 200 OK
Content-Length: 510
Content-Type: application/json
Date: Sun, 01 Apr 2018 22:47:53 GMT
Server: akka-http/10.0.11

[
    {
        "apply-url": "http://www.example.com/apply",
        "apr": 19.4,
        "card-score": 0.212,
        "features": [
            "Supports ApplePay",
            "Interest free purchases for 1 month"
        ],
        "name": "ScoredCard Builder",
        "provider": "ScoredCards"
    },
    {
        "apply-url": "http://www.example.com/apply",
        "apr": 21.4,
        "card-score": 0.137,
        "features": [],
        "name": "SuperSaver Card",
        "provider": "CSCards"
    },
    {
        "apply-url": "http://www.example.com/apply",
        "apr": 19.2,
        "card-score": 0.135,
        "features": [
            "Interest free purchases for 6 months"
        ],
        "name": "SuperSpender Card",
        "provider": "CSCards"
    }
]
```  

## Application Deployment

Application can be packages and deployed using [sbt-native-packager](http://sbt-native-packager.readthedocs.io/en/latest/introduction.html) plugin

