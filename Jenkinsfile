pipeline {
    agent any

    tools {
        maven 'Maven 3' // Adjust to your Maven installation name in Jenkins
        jdk 'JDK11'     // Adjust to the JDK version required by your project
    }

    environment {
        // Since we're using Docker Hub, we can leave DOCKER_REGISTRY empty
        DOCKER_REGISTRY = ''
        DOCKERHUB_CREDENTIALS_ID = 'docker-hub-credentials' // Update this to match your Jenkins credentials ID
        APP_NAME = 'devops-validation' // Your Docker Hub repository name
        DOCKER_IMAGE = "radhouene101/${APP_NAME}:${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out the repository...'
                git url: 'https://github.com/radhouene101/event-Ops.git', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                echo 'Building the project with Maven...'
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package'
            }
        }
        stage('Build and Push Docker Image') {
            when {
                expression {
                    fileExists('Dockerfile')
                }
            }
            steps {
                script {
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASS'
                        )
                    ]) {
                        echo "Building Docker image: ${DOCKER_IMAGE}"
                        sh "docker build -t ${DOCKER_IMAGE} ."
                        echo "Logging into Docker Hub..."
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        echo "Pushing Docker image: ${DOCKER_IMAGE}"
                        sh "docker push ${DOCKER_IMAGE}"
                        echo "Logging out from Docker Hub..."
                        sh "docker logout"
                    }
                }
            }
        }
        stage('Archive Artifacts') {
            steps {
                echo 'Archiving build artifacts...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}
