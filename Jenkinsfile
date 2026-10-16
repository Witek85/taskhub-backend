pipeline {
    agent any

    tools {
        nodejs 'node24'
    }

    environment {
        POSTGRES_HOST = 'host.docker.internal'
        POSTGRES_PORT = '5433'
        POSTGRES_DB = 'taskhub'

        VPS_HOST = '51.83.154.177'
        VPS_USER = 'ubuntu'
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

        stage('Backend - Build & Test') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'taskhub-postgres',
                            usernameVariable: 'POSTGRES_USER',
                            passwordVariable: 'POSTGRES_PASSWORD'
                        ),
                        string(
                            credentialsId: 'taskhub-jwt-secret',
                            variable: 'JWT_SECRET'
                        )
                    ]) {
                        sh './mvnw clean verify'
                    }
                }
            }
        }

        stage('OpenAPI - Publish') {
            steps {
                archiveArtifacts(
                    artifacts: 'backend/target/openapi.json',
                    fingerprint: true
                )
            }
        }

		stage('Frontend - Install') {
			steps {
				dir('frontend') {
					sh 'npm ci'
				}
			}
		}

        stage('Frontend - OpenAPI') {
            steps {
                sh '''
                    cp \
                        backend/target/openapi.json \
                        frontend/openapi/taskhub-service/openapi.json
                '''

                dir('frontend') {
                    sh '''
                        npm run openapi:taskhub:clean
                        npm run openapi:taskhub:generate
                    '''
                }
            }
        }

		stage('Frontend - Build') {
			steps {
				dir('frontend') {
					sh 'npm run build'
				}
			}
		}

		stage('Approve Production') {
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    input(
                        message: 'Deploy TaskHub to production?',
                        ok: 'Deploy'
                    )
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                sshagent(credentials: ['taskhub-vps-ssh']) {
                    sh '''
                        set -e

                        echo "Uploading backend JAR..."

                        scp \
                            -o StrictHostKeyChecking=accept-new \
                            backend/target/taskhub-0.0.1-SNAPSHOT.jar \
                            "$VPS_USER@$VPS_HOST:/tmp/taskhub-0.0.1-SNAPSHOT.jar"

                        echo "Replacing backend JAR and restarting service..."

                        ssh \
                            -o StrictHostKeyChecking=accept-new \
                            "$VPS_USER@$VPS_HOST" \
                            '
                                set -e

                                sudo cp \
                                    /tmp/taskhub-0.0.1-SNAPSHOT.jar \
                                    /home/ubuntu/witek-project-taskhub/backend/target/taskhub-0.0.1-SNAPSHOT.jar

                                sudo chown ubuntu:ubuntu \
                                    /home/ubuntu/witek-project-taskhub/backend/target/taskhub-0.0.1-SNAPSHOT.jar

                                sudo systemctl restart taskhub-backend

                                sleep 5

                                sudo systemctl is-active --quiet taskhub-backend

                                echo "Backend service is active."
                            '
                    '''
                }
            }
        }

        stage('Backend - Smoke Test') {
            steps {
                sh '''
                    for i in $(seq 1 20); do
                        if node -e "fetch('https://taskhub.itreallyworks.pl/api/health').then(r => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"; then
                            echo "Backend smoke test passed."
                            exit 0
                        fi

                        sleep 3
                    done

                    echo "Backend smoke test failed."
                    exit 1
                '''
            }
        }

        stage('Deploy Frontend') {
            steps {
                sshagent(credentials: ['taskhub-vps-ssh']) {
                    sh '''
                        set -e

                        echo "Packing frontend..."
                        tar -C frontend/dist/frontend/browser \
                            -czf /tmp/taskhub-frontend.tar.gz .

                        echo "Uploading frontend..."
                        scp \
                            -o StrictHostKeyChecking=accept-new \
                            /tmp/taskhub-frontend.tar.gz \
                            "$VPS_USER@$VPS_HOST:/tmp/taskhub-frontend.tar.gz"

                        echo "Deploying frontend..."

                        ssh \
                            -o StrictHostKeyChecking=accept-new \
                            "$VPS_USER@$VPS_HOST" \
                            '
                                set -e

                                rm -rf /tmp/taskhub-frontend
                                mkdir -p /tmp/taskhub-frontend

                                tar -xzf /tmp/taskhub-frontend.tar.gz \
                                    -C /tmp/taskhub-frontend

                                sudo rm -rf /var/www/taskhub
                                sudo mkdir -p /var/www/taskhub

                                sudo cp -a \
                                    /tmp/taskhub-frontend/. \
                                    /var/www/taskhub/

                                sudo systemctl reload nginx

                                echo "Frontend deployed."
                            '
                    '''
                }
            }
        }

        stage('Frontend - Smoke Test') {
            steps {
                sh '''
                    for i in $(seq 1 10); do
                        if node -e "fetch('https://taskhub.itreallyworks.pl/tasks').then(r => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"; then
                            echo "Frontend smoke test passed."
                            exit 0
                        fi

                        sleep 3
                    done

                    echo "Frontend smoke test failed."
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            junit 'backend/target/surefire-reports/*.xml'
        }

        success {
            archiveArtifacts(
                artifacts: 'backend/target/*.jar',
                fingerprint: true
            )

            archiveArtifacts(
                artifacts: 'backend/target/openapi.json',
                fingerprint: true
            )

            archiveArtifacts(
                artifacts: 'frontend/dist/frontend/browser/**',
                fingerprint: true
            )

            echo 'TaskHub pipeline successful.'
        }

        failure {
            echo 'TaskHub pipeline failed.'
        }
    }
}