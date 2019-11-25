void setBuildStatus(String message, String state) {
    step([
            $class            : "GitHubCommitStatusSetter",
            reposSource       : [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/tardisone/shamu-hrs-company-service.git"],
            contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "shamu-company-daily-build"],
            errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ])
}

pipeline {
    agent { label 'slave' }
    environment {
        recipient = 'jiwenhao@easternbay.cn'
        sonarqubeScannerHome = tool name: 'Hrs Sonarqube Scanner'
    }
    stages {
        stage('sonarqube analysis') {
            steps {
                echo '---------------------------------\n' +
                        '      SonarQube analysis         ' +
                        '\n---------------------------------'
                withSonarQubeEnv('SonarQube') {
                    sh "${sonarqubeScannerHome}/bin/sonar-scanner"
                }
            }
        }
    }
}