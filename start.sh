# Save this as start.sh in your JavaProject folder
#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo "Killing anything on port 8080..."
kill $(lsof -t -i:8080) 2>/dev/null

echo "Starting backend..."
cd "$SCRIPT_DIR/backend"
mvn spring-boot:run &

echo "Waiting for backend to start..."
until curl -s http://localhost:8080/api/ping > /dev/null 2>&1; do
    sleep 1
done

echo "Backend ready! Starting JavaFX..."
cd "$SCRIPT_DIR/ExpenseTracker/expense-tracker-ui"
mvn javafx:run

#chmod  +x ~/projects/project/JavaProject/start.sh
#  ./start.sh