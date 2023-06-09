#!/usr/bin/env bash

: ${USER_HOST=localhost}
: ${USER_PORT=8081}
: ${USER_PATH=api/v1/users}
: ${POST_HOST=localhost}
: ${POST_PORT=8082}
: ${POST_PATH=api/v1/posts}

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, response: $RESPONSE)"
    fi
  else
    echo "Test failed, expected HTTP Code: $expectedHttpCode, actual HTTP Code: $httpCode, will abort!"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test failed, expected value: $expected, actual value: $actual, will abort"
    exit 1
  fi
}

function assertNotEmpty() {
  local actual=$1

  echo "Test OK (actual value: $actual)"
}

function testUrl() {
  local url=$@

  if $url -ks -f -o /dev/null; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  local url=$@
  echo -n "Wait for: $url... "

  local n=0
  until testUrl $url; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "Done, continues..."
}

function createUser() {
  local body=$1

  curl -X POST http://$USER_HOST:$USER_PORT/$USER_PATH -H "Content-Type: application/json" --data "$body"
  echo
}

function updateUser() {
  local userId=$1
  local body=$2

  curl -X PUT http://$USER_HOST:$USER_PORT/$USER_PATH/${userId} -H "Content-Type: application/json" --data "$body"
  echo
}

function createPost() {
  local body=$1

  curl -X POST http://$POST_HOST:$POST_PORT/$POST_PATH -H "Content-Type: application/json" --data "$body"
  echo
}

function updatePost() {
  local postId=$1
  local body=$2

  curl -X PUT http://$POST_HOST:$POST_PORT/$POST_PATH/${postId} -H "Content-Type: application/json" --data "$body"
  echo
}

set -e

SECONDS=0
echo "Start tests"

echo "USER_HOST=${USER_HOST}"
echo "USER_PORT=${USER_PORT}"
echo "USER_PATH=${USER_PATH}"

echo "POST_HOST=${POST_HOST}"
echo "POST_PORT=${POST_PORT}"
echo "POST_PATH=${POST_PATH}"

if [[ $@ == *"start"* ]]; then
  echo "Starting Docker Compose environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

echo
echo Wait
waitForService curl -X GET http://$USER_HOST:$USER_PORT/$USER_PATH
waitForService curl -X GET http://$POST_HOST:$POST_PORT/$POST_PATH
echo Clean
assertCurl 204 "curl -X DELETE http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertCurl 204 "curl -X DELETE http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"

echo
echo Verify users number
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"

echo
echo Create user 1
createUser '{"id":"user1", "userName": "John Doe"}'
echo Verify user 1
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"John Doe"' "$(echo $RESPONSE | jq ".userName")"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"

echo Verify users number
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"

echo
echo Create user 2
createUser '{"id":"user2", "userName": "Harry Hoe"}'
echo Verify user 2
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"Harry Hoe"' "$(echo $RESPONSE | jq ".userName")"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"

echo Verify users number
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"

echo
echo Update user 1 name
updateUser "user1" '{"userName": "Richard Roe"}'
echo Verify user 1
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"Richard Roe"' "$(echo $RESPONSE | jq ".userName")"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"
echo Verify user 2
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"Harry Hoe"' "$(echo $RESPONSE | jq ".userName")"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Create post 1 for user 1
createPost '{"id":"post1", "userId":"user1", "text": "one"}'
echo Assert post 1
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post1 -s"
assertEqual '"post1"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"one"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '0' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Create post 2 for user 2
createPost '{"id":"post2", "userId":"user2", "text": "two"}'
echo Assert post 2
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post2 -s"
assertEqual '"post2"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"two"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Create post 3 for user 1
createPost '{"id":"post3", "userId":"user1", "text": "three"}'
echo Assert post 3
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post3 -s"
assertEqual '"post3"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"three"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 3 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '2' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Create post 4 for user 2
createPost '{"id":"post4", "userId":"user2", "text": "four"}'
echo Verify post 4
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post4 -s"
assertEqual '"post4"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"four"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 4 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '2' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '2' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Update post 1 text
updatePost "post1" '{"text": "ten"}'
echo Verify posts 1
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post1 -s"
assertEqual '"post1"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"ten"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"
echo Verify posts 2
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post2 -s"
assertEqual '"post2"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"two"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"
echo Verify post 3
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post3 -s"
assertEqual '"post3"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user1"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"three"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"
echo Verify post 4
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/post4 -s"
assertEqual '"post4"' "$(echo $RESPONSE | jq ".id")"
assertEqual '"user2"' "$(echo $RESPONSE | jq ".userId")"
assertEqual '"four"' "$(echo $RESPONSE | jq ".text")"
assertNotEmpty "$(echo $RESPONSE | jq ".postedAt")"

echo
echo Delete post 1
assertCurl 204 "curl -X DELETE http://$POST_HOST:$POST_PORT/$POST_PATH/post1 -s"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 3 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"
assertEqual '2' "$(echo $RESPONSE | jq ".postsNumber")"

echo
echo Delete user 2 with posts
assertCurl 204 "curl -X DELETE http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"

echo Verify users number
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 1 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertEqual '1' "$(echo $RESPONSE | jq ".postsNumber")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual '0' "$(echo $RESPONSE | jq ". | length")"
assertCurl 404 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"

echo
echo Delete user 1 with posts
assertCurl 204 "curl -X DELETE http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"

echo Verify users number
assertCurl 200 "curl http://$USER_HOST:$USER_PORT/$USER_PATH -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"

echo Verify posts number
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user1 -s"
assertEqual 0 "$(echo $RESPONSE | jq ". | length")"
assertCurl 404 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user1 -s"
assertCurl 200 "curl http://$POST_HOST:$POST_PORT/$POST_PATH/user/user2 -s"
assertEqual '0' "$(echo $RESPONSE | jq ". | length")"
assertCurl 404 "curl http://$USER_HOST:$USER_PORT/$USER_PATH/user2 -s"

if [[ $@ == *"stop"* ]]; then
  echo "Stopping Docker Compose environment..."
  echo "$ docker-compose down"
  docker-compose down
fi

echo "Finish tests in $SECONDS second(s)"
