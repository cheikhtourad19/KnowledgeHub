pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        JAVA_SERVICES = 'api-gateway auth-service user-service resource-service notification-service notification-client'
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
            parallel {
                stage('Build Java Microservices') {
                    steps {
                        sh '''
                            set -e
                            for service in ${JAVA_SERVICES}; do
                              echo "Building ${service}..."
                              mvn -B -f "${service}/pom.xml" clean install -DskipTests
                              echo "SUCCESS: ${service} built"
                            done
                        '''
                    }
                }

                stage('Build AI Service Dependencies') {
                    steps {
                        sh '''
                            set -e
                            python3 -m venv "${AI_VENV}"
                            . "${AI_VENV}/bin/activate"
                            python -m pip install --upgrade pip
                            pip install -r "${AI_DIR}/requirements.txt"
                            echo "SUCCESS: AI service dependencies built"
                        '''
                    }
                }
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

                        if command -v sonar-scanner >/dev/null 2>&1; then
                          echo "Running SonarQube analysis for AI service..."
                          sonar-scanner \
                            -Dsonar.projectKey=knowledgehub-ai-search-service \
                            -Dsonar.projectName=KnowledgeHub-AI-Search-Service \
                            -Dsonar.sources=${AI_DIR} \
                            -Dsonar.exclusions=**/__pycache__/**,**/*.pyc,**/chroma_db/**
                          echo "SUCCESS: AI service SonarQube analysis completed"
                        else
                          echo "WARNING: sonar-scanner not installed. Skipping AI service analysis."
                        fi
                    '''
                }
            }
        }

        stage('Start Infrastructure') {
            steps {
                sh '''
                    set -e
                    echo "Starting infrastructure services..."
                    docker compose up -d mysql mongodb minio
                    echo "SUCCESS: Infrastructure services started"

                    echo "Waiting for MySQL to be ready..."
                    timeout 60 sh -c 'until docker compose exec -T mysql mysqladmin ping -h localhost --silent; do sleep 2; done'

                    echo "Waiting for MongoDB to be ready..."
                    timeout 60 sh -c 'until docker compose exec -T mongodb mongosh --eval "db.adminCommand(\'ping\')" --quiet; do sleep 2; done'

                    echo "SUCCESS: All infrastructure services are healthy"
                '''
            }
        }

        stage('Launch Microservices') {
            steps {
                sh '''
                    set -e
                    mkdir -p logs

                    echo "Starting Java microservices..."
                    docker compose up -d api-gateway auth-service user-service resource-service notification-service
                    echo "SUCCESS: Java microservices started"

                    echo "Starting AI service..."
                    . "${AI_VENV}/bin/activate"
                    cd "${AI_DIR}"
                    nohup python3 main.py > ../logs/ai-search-service.log 2>&1 &
                    echo $! > ../ai-search-service.pid
                    cd ..

                    sleep 5
                    if [ -f ai-search-service.pid ] && kill -0 "$(cat ai-search-service.pid)" 2>/dev/null; then
                        echo "SUCCESS: AI service started (PID: $(cat ai-search-service.pid))"
                    else
                        echo "ERROR: AI service failed to start"
                        exit 1
                    fi
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