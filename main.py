from fastapi import FastAPI, Form
from fastapi.middleware.cors import CORSMiddleware
import requests

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/login")
def login(userId: str = Form(...), userPwd: str = Form(...)):
    login_url = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"
    payload = {
        "siteId": "cncschool",
        "returnUrl": "",
        "referer": "/cncschool/index.do",
        "inputUserId": userId,
        "inputUserPwd": userPwd,
    }

    session = requests.Session()
    res = session.post(login_url, data=payload, allow_redirects=False)

    cookies = session.cookies.get_dict()
    location = res.headers.get("Location", "")
    success = "JSESSIONID" in cookies and "index.do" in location

    if success:
        return {
            "success": True,
            "session_cookie": cookies["JSESSIONID"],
            "redirect": location
        }
    else:
        return {
            "success": False,
            "message": "로그인 실패",
            "redirect": location
        }
