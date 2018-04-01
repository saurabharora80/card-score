#!/bin/bash

function random_port {
    python -c 'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1]); s.close()'
}

export APP_PORT=`random_port`

sbt stage
target/universal/stage/bin/card-score -DHTTP_PORT=${APP_PORT} 1> card-score-std.log 2> card-score-error.log &

while ! lsof -t -i:${APP_PORT} ; do sleep 1; done

echo "Server online at http://localhost:${APP_PORT}/"
echo "Standard Output => ./card-score-std.log; Standard Error => card-score-error.log"