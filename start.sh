#!/bin/bash

echo "========================================"
echo "  BANKNET - Démarrage avec Docker"
echo "========================================"
echo ""

echo "[1/3] Démarrage du conteneur MySQL Docker..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "ERREUR: Impossible de démarrer Docker. Vérifiez que Docker est lancé."
    exit 1
fi

echo ""
echo "[2/3] Attente du démarrage de MySQL (10 secondes)..."
sleep 10

echo ""
echo "[3/3] Lancement de l'application BANKNET..."
echo ""
mvn clean compile javafx:run



