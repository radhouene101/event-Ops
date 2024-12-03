pipeline {
    agent any

    tools {
        maven 'M2_HOME'
        jdk 'JAVA_HOME'
    }

    environment {
        DOCKER_REGISTRY = '' // Docker Hub Registry
        APP_NAME = 'devops-validation' // Docker Image Name
        DOCKER_IMAGE = "radhouene101/${APP_NAME}:${env.BUILD_NUMBER}"
        NEXUS_VERSION = "nexus3"
        NEXUS_URL = "192.168.30.186:8088"
        NEXUS_CREDENTIALS = 'nexus'
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
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube1') {
                    sh 'mvn sonar:sonar -X'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                script {
                    nexusArtifactUploader(
                        nexusVersion: NEXUS_VERSION,
                        protocol: 'http',
                        nexusUrl: "${NEXUS_URL}/repository/",
                        groupId: 'tn.esprit',
                        artifactId: 'eventsProject',
                        version: '1.0.0-SNAPSHOT',
                        repository: 'maven-snapshots',
                        credentialsId: NEXUS_CREDENTIALS,
                        artifacts: [[artifactId: 'eventsProject', file: 'target/eventsProject-1.0.0-SNAPSHOT.jar', type: 'jar']]
                    )
                }
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
                        usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')
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
