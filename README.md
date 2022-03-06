# 기억 공간 프로젝트 API 서버
기억 공간(OurMemory) 프로젝트에서 클라이언트(안드로이드, iOS)와 통신하고 데이터를 관리할 API서버를 만든다.

# 목차
1. [환경](#1-환경)
2. [개요](#2-개요)
3. [CI/CD](#3-CI/CD)
4. [주요기능](#4-주요기능)
5. [ERD](#5-ERD)
6. [프로젝트 문서](#6-프로젝트-문서)

## 1. 환경
  * JDK : 16
  * Spring Boot 2.5.6 / gradle
  * DB : MySQL8 / Spring data JPA

## 2. 개요
클라이언트(iOS, Android) 로부터 요청을 받아 처리하기 위한 API 서버

## 3. CI/CD
1. GitHub Action
  개발 브랜치(develop) 로 이슈 처리 완료된 코드가 병합되어 푸시되는 경우, GitHub Action 을 실행합니다.
  bootJar 를 통해 생성된 패키지 파일 및 CodeDeploy 실행 순서 appspec.yml, 대상 서버에 배포 후 서버를 구동하기 위한 scripts/ 디렉토리를 압축(Archive.tgz)하여 S3 에 업로드합니다.
  이후 CodeDeploy 에 업로드된 파일에 대해 배포 요청합니다.
2. CodeDeploy
  S3 에서 파일을 받아 대상 서버(EC2)에 업로드합니다.
  이후, 대상 파일을 압축해제한 뒤 appspec.yml 에 내용에 따라 각 이벤트 별 스크립트를 실행합니다.
3. EC2
  CodeDeploy 배포대상 서버입니다.
  스크립트를 통해 최초 배포된 파일들을 위치시키고, 서버를 기동합니다.
  서버 구동에 필요한 설정 파일의 경우, 보안을 위해 자동 배포에서 제외되기 때문에 수동으로 업로드하여 관리하고 있습니다.

![image](https://user-images.githubusercontent.com/43669379/156932367-dfe219e3-b2d8-4e27-a376-0512e4729ad1.png)


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
