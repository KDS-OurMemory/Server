# 기억 공간 프로젝트 API 서버
기억 공간(OurMemory) 프로젝트에서 클라이언트(안드로이드, iOS)와 통신하고 데이터를 관리할 API서버를 만든다.

# 목차
1. [환경](#1-환경)
2. [개요](#2-개요)
3. [주요기능](#3-주요기능)
4. [ERD](#4-ERD)
5. [CI&CD](#5-CI&CD)
6. [프로젝트 문서](#6-프로젝트-문서)

## 1. 환경
  * JDK : 1.15
  * Spring Boot 2.4.2 / gradle
  * DB : MySQL8 / Spring data JPA

## 2. 개요
클라이언트(iOS, Android) 로부터 요청을 받아 처리하기 위한 서버

## 3. 주요기능
일정을 자유롭게 작성하고 필요한 경우, 일정 참여자 혹은 방에 공유할 수 있습니다.
  
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

    `참여자가 전부 포함된 방이 있다면, 해당 방에 입장하여 일정을 생성해보세요. 입장한 방에 일정이 추가됩니다.`

## 4. ERD
https://www.erdcloud.com/d/JHJubRmhG7e2aD5cA
![ERD](https://user-images.githubusercontent.com/43669379/122242668-7bdc9200-cefe-11eb-973a-5f7fc4e4091d.png)

## 5. CI&CD
 * Jenkins http://34.64.158.97/
 * id/pwd : Guest/Guest
 * 프로젝트 : KDS-OurMemory-Server

## 6. 프로젝트 문서
  * Notion  https://www.notion.so/App-Server-22cd97ca1d92443298ca67b8a8c18f8c
