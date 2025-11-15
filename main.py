from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from bs4 import BeautifulSoup
import requests
import datetime

app = FastAPI()

# ------------------------------
# 전역 세션 (로그인 유지)
# ------------------------------
global_session = requests.Session()


# ------------------------------
# Request Models
# ------------------------------
class LoginRequest(BaseModel):
    student_id: str
    password: str


class ReservationRequest(BaseModel):
    spaceSeq: int
    spaceName: str
    date: str          # YYYY-MM-DD
    timeList: list[str]
    tel: str
    email: str


# ------------------------------
# 로그인 API
# ------------------------------
@app.post("/login")
def login(req: LoginRequest):
    login_url = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"

    payload = {
        "id": req.student_id,
        "pw": req.password
    }

    res = global_session.post(login_url, data=payload, allow_redirects=False)

    if "JSESSIONID" not in global_session.cookies.get_dict():
        raise HTTPException(status_code=401, detail="Login failed")

    return {"success": True}


# ----------------------------------------------------
# 실제 예약 API — 예약 페이지에서 hidden 값 자동 파싱
# ----------------------------------------------------
@app.post("/reserve")
def reserve(req: ReservationRequest):

    # 1) 로그인 여부 확인
    if "JSESSIONID" not in global_session.cookies.get_dict():
        raise HTTPException(status_code=401, detail="Not logged in")

    # 2) 예약 페이지 불러오기 (hidden 값 파싱 위해)
    page_url = "https://hansung.ac.kr/cncschool/12195/subview.do"
    page_res = global_session.get(page_url)

    if page_res.status_code != 200:
        raise HTTPException(status_code=400, detail="Cannot load reservation page")

    soup = BeautifulSoup(page_res.text, "html.parser")

    # 3) actionForm 안의 모든 hidden input 가져오기
    form_data = {}
    hidden_inputs = soup.select("form[name='actionForm'] input[type='hidden']")

    for inp in hidden_inputs:
        name = inp.get("name")
        value = inp.get("value", "")
        if name:
            form_data[name] = value

    # ------------------------------
    # 날짜 → 요일 변환
    # ------------------------------
    yoils = ["월", "화", "수", "목", "금", "토", "일"]
    dt = datetime.datetime.strptime(req.date, "%Y-%m-%d")
    sel_day = yoils[dt.weekday()]

    # ------------------------------
    # 동적으로 입력값 덮어쓰기
    # ------------------------------
    form_data["siteId"] = "cncschool"
    form_data["fnctNo"] = "40"
    form_data["groupNm"] = "코딩 라운지"
    form_data["spceNm"] = req.spaceName
    form_data["selDay"] = sel_day

    form_data["resveDeStr"] = req.date
    form_data["resveSpceSeq"] = str(req.spaceSeq)
    form_data["telno"] = req.tel
    form_data["email"] = req.email

    results = []
    reserve_url = "https://hansung.ac.kr/resve/cncschool/40/artclRegist.do"

    # ----------------------------------------------------
    # 여러 시간대 예약 처리 — 하나씩 예약 요청 보내기
    # ----------------------------------------------------
    for t in req.timeList:
        send_data = form_data.copy()
        send_data["resveTm"] = t

        res = global_session.post(reserve_url, data=send_data, allow_redirects=False)

        if res.status_code == 302:
            results.append({
                "time": t,
                "success": True,
                "status": res.status_code,
                "location": res.headers.get("Location")
            })
        else:
            results.append({
                "time": t,
                "success": False,
                "status": res.status_code,
                "text": res.text[:300]
            })

    return {"results": results}


# ------------------------------
# 공간 목록 가져오기
# ------------------------------
@app.get("/spaces")
def get_spaces():
    url = "https://hansung.ac.kr/cncschool/12195/subview.do"

    res = global_session.get(url)
    soup = BeautifulSoup(res.text, "html.parser")

    options = soup.find_all("option")

    space_list = []

    for op in options:
        if op.get("value") and "호" in op.text:
            space_list.append({
                "spaceSeq": int(op.get("value")),
                "spaceName": op.text.strip()
            })

    return {"spaces": space_list}


# ------------------------------
# 예약된 시간 목록 조회
# ------------------------------
@app.get("/disabled-times")
def get_disabled_times(date: str, spaceSeq: int):

    url = "https://hansung.ac.kr/resve/cncschool/40/resveList.do"

    payload = {
        "resveSpceSeq": str(spaceSeq),
        "resveDeStr": date
    }

    res = global_session.post(url, data=payload)

    disabled = []
    try:
        data = res.json()
        disabled = [item.get("resveTm") for item in data if "resveTm" in item]
    except:
        disabled = []

    return {"disabled": disabled}
