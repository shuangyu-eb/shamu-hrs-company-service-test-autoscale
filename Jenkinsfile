void setBuildStatus(String message, String state) {
    step([
            $class            : "GitHubCommitStatusSetter",
            reposSource       : [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/tardisone/shamu-hrs-company-service.git"],
            contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "shamu-company-service-master-build"],
            errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ])
}

pipeline {
    agent { label 'slave' }
    environment {
        recipient = 'jiwenhao@easternbay.cn'
        sonarqubeScannerHome = tool name: 'Shamu Hrs Company Service Sonarqube Scanner'
    }
    stages {
        stage('unit tests') {
                    steps {
                        echo '---------------------------------\n' +
                                '            Unit test            ' +
                                '\n---------------------------------'

                        sh 'mvn clean test -Dspring.profiles.active=test > junit_output.txt'
                    }
                }
        stage('sonarqube analysis') {
            steps {
                echo '---------------------------------\n' +
                        '      SonarQube analysis         ' +
                        '\n---------------------------------'
                withSonarQubeEnv('ShamuHrsCompanyServiceSonarQube') {
                    sh "${sonarqubeScannerHome}/bin/sonar-scanner"
                }
            }
        }
    }
}
