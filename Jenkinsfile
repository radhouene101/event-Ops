pipeline {
    agent any

    tools {
        maven 'M2_HOME' // Ensure these match the names in Jenkins Global Tool Configuration
        jdk 'JAVA_HOME'
    }

    environment {
        DOCKER_REGISTRY = '' // For Docker Hub
        APP_NAME = 'devops-validation' // Your Docker Hub repository name
        DOCKER_IMAGE = "radhouene101/${APP_NAME}:${env.BUILD_NUMBER}"
        NEXUS_URL = 'http://192.168.30.186:8088/' // Replace with your Nexus URL
        NEXUS_CREDENTIALS = 'nexus' // ID of Nexus credentials in Jenkins
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
                junit 'target/surefire-reports/*.xml' // Publish test results
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube1') { // Replace 'MySonarQube' with your SonarQube server name in Jenkins
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
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package'
            }
        }
        stage('Upload to Nexus') {
            steps {
                script {
                    nexusArtifactUploader artifacts: [[artifactId: 'devops-validation',
                                                      classifier: '',
                                                      file: 'target/eventsProject-1.0.0-SNAPSHOT.jar',
                                                      type: 'jar']],
                                          credentialsId: "${NEXUS_CREDENTIALS}",
                                          groupId: 'tn.esprit', // Replace with your group ID
                                          nexusUrl: "${NEXUS_URL}",
                                          repository: 'maven-releases', // Replace with your Nexus repository name
                                          version: '1.0'
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
