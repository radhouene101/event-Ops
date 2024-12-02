pipeline {
    agent any

    tools {
            maven 'M2_HOME' // Adjust to your Maven installation name in Jenkins
            jdk 'JAVA_HOME' // Adjust to the JDK version required by your project
        }

    environment {
        APP_NAME = 'event-ops' // Name of your application
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
                        string(credentialsId: 'docker-registry-url', variable: 'DOCKER_REGISTRY'),
                        usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')
                    ]) {
                        def imageName = "${DOCKER_REGISTRY}/${APP_NAME}:${env.BUILD_NUMBER}"
                        echo "Building Docker image: ${imageName}"
                        sh "docker build -t ${imageName} ."
                        echo "Pushing Docker image: ${imageName}"
                        sh "echo $DOCKER_PASS | docker login ${DOCKER_REGISTRY} -u $DOCKER_USER --password-stdin"
                        sh "docker push ${imageName}"
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
