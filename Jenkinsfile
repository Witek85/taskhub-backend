
pipeline {
    agent any

    tools {
        nodejs 'node24'
    }

    environment {
        POSTGRES_HOST = 'host.docker.internal'
        POSTGRES_PORT = '5433'
        POSTGRES_DB = 'taskhub_ci'

        VPS_HOST = '51.83.154.177'
        VPS_USER = 'ubuntu'

        JAR_NAME = 'taskhub-0.0.1-SNAPSHOT.jar'
        DEPLOY_DIR = '/home/ubuntu/taskhub-backend/target'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'taskhub-postgres-ci',
                        usernameVariable: 'POSTGRES_USER',
                        passwordVariable: 'POSTGRES_PASSWORD'
                    ),
                    string(
                        credentialsId: 'taskhub-jwt-secret',
                        variable: 'JWT_SECRET'
                    )
                ]) {
                       sh '''
                           SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
                           ./mvnw clean verify
                       '''
                   }
            }
        }

        stage('Publish OpenAPI') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/openapi.json',
                    fingerprint: true
                )
            }
        }

        stage('Deploy Backend') {
            steps {
                sshagent(credentials: ['taskhub-vps-ssh']) {
                    sh '''
                        set -e

                        scp -o StrictHostKeyChecking=accept-new \
                            "target/$JAR_NAME" \
                            "$VPS_USER@$VPS_HOST:/tmp/$JAR_NAME"

                        ssh -o StrictHostKeyChecking=accept-new \
                            "$VPS_USER@$VPS_HOST" \
                            "sudo install -m 644 /tmp/$JAR_NAME $DEPLOY_DIR/$JAR_NAME.new && \
                             sudo mv $DEPLOY_DIR/$JAR_NAME.new $DEPLOY_DIR/$JAR_NAME && \
                             sudo systemctl restart taskhub-backend && \
                             sudo systemctl is-active --quiet taskhub-backend"
                    '''
                }
            }
        }

        stage('Smoke Test') {
            steps {
                sh '''
                    for i in $(seq 1 20); do
                        if node -e "fetch('https://taskhub.itreallyworks.pl/api/health').then(r => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"; then
                            echo "Backend health check passed."
                            exit 0
                        fi

                        sleep 3
                    done

                    echo "Backend health check failed."
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/*.xml'
        }

        success {
            archiveArtifacts(
                artifacts: 'target/*.jar',
                fingerprint: true
            )
            echo 'TaskHub backend pipeline successful.'
        }

        failure {
            echo 'TaskHub backend pipeline failed.'
        }
    }
}
