# 🐳 Guide Docker pour BANKNET

## Prérequis
- Docker Desktop installé et démarré
- Docker Compose installé (généralement inclus avec Docker Desktop)

## Démarrage rapide

### 1. Démarrer le conteneur MySQL
```bash
docker-compose up -d
```

Cette commande va :
- Créer et démarrer un conteneur MySQL 8.0
- Créer la base de données `banknet_db`
- Créer l'utilisateur `bank_user` avec le mot de passe `bank_pass`
- Exposer MySQL sur le port 3306

### 2. Vérifier que le conteneur est démarré
```bash
docker-compose ps
```

Vous devriez voir le conteneur `banknet_mysql` en état "Up".

### 3. Lancer l'application
```bash
mvn clean compile javafx:run
```

L'application va :
- Attendre que MySQL soit prêt (jusqu'à 20 secondes)
- Initialiser automatiquement la base de données si elle est vide
- Créer un compte admin (admin / admin123)
- Créer un client de test avec un compte bancaire de 1000€

## Comptes par défaut

### Administrateur
- **Login** : `admin`
- **Password** : `admin123`

### Client de test
- **Login** : `client1`
- **Password** : `client123`
- **CIN** : `AB123456`
- **Compte bancaire** : Solde initial de 1000.00 €

## Commandes utiles

### Arrêter le conteneur
```bash
docker-compose down
```

### Arrêter et supprimer les données
```bash
docker-compose down -v
```

### Voir les logs MySQL
```bash
docker-compose logs mysql
```

### Redémarrer le conteneur
```bash
docker-compose restart
```

## Configuration

Les paramètres de connexion sont configurés dans :
- `docker-compose.yml` : Configuration du conteneur MySQL
- `src/main/resources/hibernate.cfg.xml` : Configuration Hibernate

### Modifier les identifiants

1. Dans `docker-compose.yml`, modifiez :
   ```yaml
   MYSQL_USER: votre_user
   MYSQL_PASSWORD: votre_password
   ```

2. Dans `src/main/resources/hibernate.cfg.xml`, modifiez :
   ```xml
   <property name="hibernate.connection.username">votre_user</property>
   <property name="hibernate.connection.password">votre_password</property>
   ```

3. Redémarrez le conteneur :
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

## Dépannage

### Le conteneur ne démarre pas
```bash
# Vérifier les logs
docker-compose logs mysql

# Vérifier que le port 3306 n'est pas déjà utilisé
netstat -an | findstr 3306
```

### La connexion échoue
1. Vérifiez que Docker est démarré
2. Vérifiez que le conteneur est actif : `docker-compose ps`
3. Attendez quelques secondes après le démarrage pour que MySQL soit prêt
4. Vérifiez les logs : `docker-compose logs mysql`

### Réinitialiser complètement la base de données
```bash
docker-compose down -v
docker-compose up -d
# Attendez 10 secondes
mvn clean compile javafx:run
```

## Structure des volumes

Les données MySQL sont persistées dans un volume Docker nommé `mysql_data`. 
Même si vous arrêtez le conteneur, les données sont conservées.

Pour supprimer complètement les données, utilisez :
```bash
docker-compose down -v
```



