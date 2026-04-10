pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        JAVA_SERVICES = 'notification-client api-gateway auth-service user-service resource-service notification-service'
        SONARQUBE_ENV = 'sonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "SUCCESS: Checkout completed"
            }
        }

        stage('Build') {
            steps {
                sh '''
                    set -e

                    echo "Installing parent POM..."
                    mvn -B -f pom.xml install -N
                    echo "SUCCESS: parent POM installed"

                    for service in ${JAVA_SERVICES}; do
                      echo "Building ${service}..."
                      mvn -B -f "${service}/pom.xml" clean install -DskipTests
                      echo "SUCCESS: ${service} built"
                    done
                '''
            }
        }

        stage('Start Test Dependencies') {
            steps {
                sh '''
                    set -e
                    echo "Starting test dependencies (MySQL, MongoDB, MinIO)..."
                    docker compose -f docker-compose.yml up mysql mongodb minio -d

                    echo "Starting SonarQube..."
                    docker compose -f docker-compose.sonarqube.yml up -d

                    echo "All test dependencies are ready!"
                '''
            }
        }

        stage('Test Java Microservices') {
            steps {
                sh '''
                    set -e
                    for service in ${JAVA_SERVICES}; do
                      echo "Testing ${service}..."
                      mvn -B -f "${service}/pom.xml" test
                      echo "SUCCESS: ${service} tests passed"
                    done
                '''
            }
        }

        stage('Stop Test Dependencies') {
            steps {
                sh '''
                    echo "Stopping test dependencies..."
                    docker compose -f docker-compose.yml down -v
                    echo "Test dependencies stopped"
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '''
                        set -e
                        echo "Running SonarQube analysis..."
                        mvn -B -f pom.xml sonar:sonar \
                          -Dsonar.projectKey=knowledgehub-java-services \
                          -Dsonar.projectName=KnowledgeHub-Java-Services
                        echo "SUCCESS: SonarQube analysis completed"
                    '''
                }
            }
        }

        stage('Deploy Services') {
            steps {
                sh '''
                    set -e
                    mkdir -p logs

                    echo "Stopping any previously running services..."
                    docker compose -f docker-compose.yml down -v || true

                    echo "Building and starting updated services..."
                    docker compose -f docker-compose.yml build
                    docker compose -f docker-compose.yml up -d

                    echo "SUCCESS: Services are up and running"
                '''
            }
        }
    }

    post {
        always {
            sh '''
                set +e
                echo "Current service status:"
                docker compose -f docker-compose.yml ps || true
            '''
            archiveArtifacts allowEmptyArchive: true, artifacts: 'logs/**/*.log'
        }

        success {
            echo "========================================="
            echo "         PIPELINE SUCCEEDED"
            echo "========================================="
        }

        failure {
            sh '''
                set +e
                echo "Pipeline failed — tearing down all services..."
                docker compose -f docker-compose.yml down -v || true
                docker compose -f docker-compose.sonarqube.yml down || true
            '''
            echo "========================================="
            echo "         PIPELINE FAILED"
            echo "========================================="
        }
    }
}