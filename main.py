from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests

app = FastAPI(title="HansungHub FastAPI Server")

LOGIN_URL = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"
RESERVE_URL = "https://hansung.ac.kr/facility/reserve"  # (예시용 URL, 실제는 바꿔야 함)

# ✅ 로그인 요청 Body
class LoginItem(BaseModel):
    studentId: str
    password: str

# ✅ 예약 요청 Body
class ReserveItem(BaseModel):
    sessionId: str
    facility: str
    time: str


@app.post("/login")
def login(item: LoginItem):
    session = requests.Session()
    payload = {
        "id": item.studentId,
        "pwd": item.password
    }

    res = session.post(LOGIN_URL, data=payload)
    cookies = session.cookies.get_dict()

    # 로그인 성공 판별
    if "JSESSIONID" in cookies:
        return {
            "success": True,
            "sessionId": cookies["JSESSIONID"]
        }
    else:
        return {"success": False}


@app.post("/reserve")
def reserve(item: ReserveItem):
    # 클라이언트로부터 전달받은 sessionId 사용
    headers = {"Cookie": f"JSESSIONID={item.sessionId}"}

    payload = {
        "facility": item.facility,
        "time": item.time
    }

    res = requests.post(RESERVE_URL, headers=headers, data=payload)

    if res.status_code == 200:
        return {"success": True, "message": "예약 성공", "response": res.text}
    else:
        raise HTTPException(status_code=400, detail="예약 실패")
