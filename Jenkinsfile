pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        JAVA_SERVICES = 'notification-client api-gateway auth-service user-service resource-service notification-service '
        AI_DIR = 'ai-and-search-service'
        AI_VENV = '.venv-ai'
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
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '''
                        set -e

                        echo "Running SonarQube analysis for Java services..."
                        mvn -B -pl api-gateway,auth-service,user-service,resource-service,notification-service,notification-client -am sonar:sonar \
                          -Dsonar.projectKey=knowledgehub-java-services \
                          -Dsonar.projectName=KnowledgeHub-Java-Services
                        echo "SUCCESS: Java services SonarQube analysis completed"


                    '''
                }
            }
        }



        stage('Build and Start Services') {
            steps {
                sh '''
                    set -e
                    mkdir -p logs

                    echo "Starting Java microservices..."
                    sudo docker compose build && docker compose up -d
                    echo "SUCCESS: Java microservices started"


                '''
            }
        }
    }

    post {
        always {
            sh '''
                set +e

                if [ -f ai-search-service.pid ]; then
                  echo "Stopping AI service..."
                  kill "$(cat ai-search-service.pid)" || true
                  rm -f ai-search-service.pid
                  echo "AI service stopped"
                fi

                echo "Current service status:"
                docker compose ps || true
                docker compose down || true
            '''

            archiveArtifacts allowEmptyArchive: true, artifacts: 'logs/**/*.log'
        }

        success {
            echo "========================================="
            echo "         PIPELINE SUCCEEDED"
            echo "========================================="
        }

        failure {
            echo "========================================="
            echo "         PIPELINE FAILED"
            echo "========================================="
        }
    }
}