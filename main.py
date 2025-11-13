from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests

app = FastAPI(title="HansungHub FastAPI Server")

LOGIN_URL = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"


class LoginItem(BaseModel):
    studentId: str
    password: str


class LoginResponse(BaseModel):
    success: bool
    sessionId: str | None = None


@app.post("/login", response_model=LoginResponse)
def login(item: LoginItem):
    session = requests.Session()

    # ✔ 학교 서버가 요구하는 정확한 Form Data
    payload = {
        "siteId": "cncschool",
        "returnUrl": "/cncschool/index.do",
        "referer": "/cncschool/index.do",
        "inputUserId": item.studentId,
        "inputUserPwd": item.password
    }

    # ✔ 학교 서버와 비슷한 헤더 추가 (중요!)
    headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "User-Agent": "Mozilla/5.0",
        "Referer": "https://hansung.ac.kr/hnuLogin/cncschool/loginView.do",
    }

    try:
        res = session.post(LOGIN_URL, data=payload, headers=headers)
    except Exception:
        raise HTTPException(status_code=500, detail="학교 로그인 서버와 통신 실패")

    html = res.text
    print("===== HTML RESPONSE =====")
    print(html)

    # ✔ 실패 문구 체크
    if "회원정보이(가) 일치하지 않습니다" in html:
        return LoginResponse(success=False, sessionId=None)

    # ✔ 성공 시 쿠키 추출
    cookies = session.cookies.get_dict()
    if "JSESSIONID" in cookies:
        return LoginResponse(success=True, sessionId=cookies["JSESSIONID"])

    return LoginResponse(success=False, sessionId=None)
