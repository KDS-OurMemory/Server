# 기억 공간 프로젝트 API 서버
기억 공간(OurMemory) 프로젝트에서 클라이언트(안드로이드, iOS)와 통신하고 데이터를 관리할 API서버를 만든다.

# 목차
1. [환경](#1-환경)
2. [개요](#2-개요)
3. [CI/CD](#3-cicd)
4. [주요기능](#4-주요기능)
5. [ERD](#5-erd)
6. [프로젝트 문서](#6-프로젝트-문서)

## 1. 환경
  * Spring Boot 2.5.6 / gradle / JDK 16
  * DB : MySQL8(RDS) / Spring data JPA
  * 버전관리 : [메이저 버전].[마이너 버전].[이슈 버전]
    - 메이저 버전 : 정식 배포된 경우 증가
    - 마이너 버전 : 이슈 버전이 많이 증가하여 분기가 필요한 경우, 증가
    - 이슈 버전 : 이슈가 수정될 때마다 증가
  * 브랜치 전략(GitHub Flow)
    1. 이슈 브랜치 생성 및 작업 진행 
       - 이슈 브랜치 푸시할 경우 `build` 진행(feat. `GitHub Action`)
    2. develop 브랜치로 Pull request 요청 
       - 풀 리퀘스트 요청 시, `build` 진행(feat. `GitHub Action`)
    3. develop 브랜치에 병합 
       - 병합된 코드 기준 `build` & `deploy` 진행(feat. `GitHub Action`, `CodeDeploy`)

## 2. 개요
클라이언트(iOS, Android) 로부터 요청을 받아 처리하기 위한 API 서버

## 3. CI/CD
  1. GitHub Action
     - `Github Action이란 Github 저장소를 기반으로 소프트웨어 개발 Workflow를 자동화 할 수 있는 CI/CD 도구입니다.`
     - 이슈 브랜치 push, 개발 브랜치(develop) pull request 하는 경우 GitHub Action 을 통해 build 를 진행합니다.
     - 개발 브랜치(develop) 로 이슈 브랜치가 병합되는 경우, deploy 를 진행합니다. 과정은 아래와 같습니다.
        1. 병합된 브랜치(develop) 기준 build 테스트
        2. 빌드된 파일(Archive.jar) 및 배포에 필요한 파일(appspec.yml, scrips) 모아서 압축(Archive.tgz)
        3. S3 에 압축된 패키지 업로드
        4. CodeDeploy 에 업로드된 패키지 배포 요청
  2. CodeDeploy
     - `AWS 서비스(EC2 인스턴스 등) 로 애플리케이션 배포를 자동화하는 배포 서비스입니다.`
     - S3 에서 파일을 받아 대상 서버(EC2)에 업로드합니다.
     - 이후, 대상 파일을 압축해제한 뒤 appspec.yml 에 내용에 따라 각 이벤트 별 스크립트를 실행합니다.
  3. EC2
     - `AWS 에서 제공하는 클라우드 기반 서버입니다.`
     - CodeDeploy 배포대상 서버로 활용하고 있습니다.
     - 스크립트를 통해 최초 배포된 파일들을 위치시키고, 서버를 기동합니다.
     - 서버 구동에 필요한 설정 파일의 경우, 보안을 위해 자동 배포에서 제외되기 때문에 수동으로 업로드하여 관리하고 있습니다.
![CI/CD 순서도](https://user-images.githubusercontent.com/43669379/157431870-ed710f3c-9ede-4987-be6d-2dd762bf588a.png)


## 4. 주요기능
일정을 자유롭게 작성하고 필요한 경우, 일정 참여자 혹은 방에 공유할 수 있습니다.
  * ### 사용자
    방을 생성하고, 일정을 생성하며 관리하는 주체가 되는 사용자입니다.
  
  * ### 친구
    사용자 간 친구 관계를 형성하여 친구들과 함께 방을 만들어 일정을 공유할 수 있습니다.
  
  * ### 방
    사용자들을 그룹화하여 모아놓은 곳입니다.

  * ### 일정
    언제, 어디서, 어떤 일을 할 지 작성합니다. 

    1. 일정 생성
      일정제목, 내용, 장소 등 구체적인 일정을 작성합니다.
    2. 일정 참여자 설정
      작성한 일정에 참여자가 있다면 설정하세요.   
      일정 참여자에게 알림이 전송되고 일정을 공유하기 위한 방이 생성됩니다.   
    3. 방에 일정 공유
      일정을 다른 방에 공유할 수 있습니다.

## 5. ERD
https://www.erdcloud.com/d/eKXiLkBGXpt5pm4Fo
![ERD](https://user-images.githubusercontent.com/43669379/139289642-b6817fe1-616d-4cfe-9fc0-4fa50331f822.png)

## 6. 프로젝트 문서
  * Notion  https://www.notion.so/App-Server-22cd97ca1d92443298ca67b8a8c18f8c
