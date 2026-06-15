import { useEffect, useMemo, useState } from "react";
import { Link, Route, Routes, useNavigate, useParams } from "react-router-dom";
import {
  createMedicine,
  deleteMedicine,
  getMedicine,
  listMedicines,
  listToday,
  updateMedicine
} from "./api.js";

const MAX_SCHEDULES = 6;

function BackButton() {
  const navigate = useNavigate();
  return (
    <button className="back-btn" type="button" onClick={() => navigate(-1)}>
      戻る
    </button>
  );
}

function formatSchedules(schedules) {
  return schedules.join(" / ");
}

function Home() {
  const [today, setToday] = useState([]);
  const [medicines, setMedicines] = useState([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setError("");
      const [todayItems, allMedicines] = await Promise.all([listToday(), listMedicines()]);
      setToday(todayItems);
      setMedicines(allMedicines);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <section className="card">
      <header className="hero">
        <p className="eyebrow">Medicine Reminder</p>
        <h1>お薬リマインダー</h1>
        <p className="subtitle">服薬時間を設定して、LINE通知の準備をしましょう。</p>
      </header>

      <div className="phase-note">
        Phase 1: お薬の登録と服薬時間の設定（LINEログインは Phase 2）
      </div>

      {error && <p className="error">{error}</p>}

      <div className="toolbar">
        <Link to="/medicines/new" className="button">
          + お薬を登録
        </Link>
        <Link to="/medicines" className="button secondary">
          お薬一覧
        </Link>
      </div>

      <section className="panel">
        <h2>今日の服薬予定</h2>
        {today.length === 0 ? (
          <p className="empty">今日の服薬予定はありません。</p>
        ) : (
          <ul className="schedule-list">
            {today.map((item) => (
              <li key={`${item.medicineId}-${item.time}`}>
                <span className="time-badge">{item.time}</span>
                <div>
                  <strong>{item.medicineName}</strong>
                  <p>{item.memo || "メモなし"}</p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel">
        <h2>登録済みのお薬</h2>
        {medicines.length === 0 ? (
          <p className="empty">まだお薬が登録されていません。</p>
        ) : (
          <ul className="medicine-list">
            {medicines.slice(0, 3).map((medicine) => (
              <li key={medicine.id}>
                <div>
                  <strong>{medicine.name}</strong>
                  <p>{formatSchedules(medicine.schedules)}</p>
                </div>
                <Link to={`/medicines/${medicine.id}`} className="small-btn">
                  詳細
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  );
}

function MedicineList() {
  const [medicines, setMedicines] = useState([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setError("");
      setMedicines(await listMedicines());
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <section className="card">
      <BackButton />
      <h2>お薬一覧</h2>
      {error && <p className="error">{error}</p>}

      <Link to="/medicines/new" className="button">
        + お薬を登録
      </Link>

      <ul className="medicine-list">
        {medicines.map((medicine) => (
          <li key={medicine.id} className={medicine.active ? "" : "inactive"}>
            <div>
              <strong>{medicine.name}</strong>
              <p>{formatSchedules(medicine.schedules)}</p>
              {!medicine.active && <span className="status-tag">停止中</span>}
            </div>
            <div className="actions">
              <Link to={`/medicines/${medicine.id}`} className="small-btn">
                詳細
              </Link>
              <Link to={`/medicines/${medicine.id}/edit`} className="small-btn">
                編集
              </Link>
              <button
                className="small-btn danger"
                type="button"
                onClick={async () => {
                  if (!window.confirm(`「${medicine.name}」を削除しますか？`)) return;
                  await deleteMedicine(medicine.id);
                  load();
                }}
              >
                削除
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}

function emptyForm() {
  return { name: "", memo: "", schedules: ["08:00"], active: true };
}

function MedicineForm({ isEdit }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm());
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isEdit) return;
    getMedicine(id)
      .then((medicine) => {
        setForm({
          name: medicine.name,
          memo: medicine.memo || "",
          schedules: medicine.schedules.length > 0 ? medicine.schedules : ["08:00"],
          active: medicine.active
        });
      })
      .catch((err) => setError(err.message));
  }, [id, isEdit]);

  const title = useMemo(() => (isEdit ? "お薬を編集" : "お薬を登録"), [isEdit]);

  const addSchedule = () => {
    if (form.schedules.length >= MAX_SCHEDULES) return;
    setForm({ ...form, schedules: [...form.schedules, "12:00"] });
  };

  const removeSchedule = (index) => {
    if (form.schedules.length <= 1) return;
    setForm({
      ...form,
      schedules: form.schedules.filter((_, i) => i !== index)
    });
  };

  const updateSchedule = (index, value) => {
    const schedules = [...form.schedules];
    schedules[index] = value;
    setForm({ ...form, schedules });
  };

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const payload = {
        name: form.name.trim(),
        memo: form.memo.trim(),
        schedules: form.schedules,
        active: form.active
      };
      if (isEdit) {
        await updateMedicine(id, payload);
      } else {
        await createMedicine(payload);
      }
      navigate("/medicines");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="card">
      <BackButton />
      <h2>{title}</h2>
      <p className="subtitle">1日あたり最大 {MAX_SCHEDULES} 回まで設定できます。</p>
      {error && <p className="error">{error}</p>}

      <form className="form" onSubmit={submit}>
        <label>
          お薬名 *
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            maxLength={50}
            required
            placeholder="例: 降圧剤"
          />
        </label>

        <label>
          メモ（任意）
          <textarea
            value={form.memo}
            onChange={(e) => setForm({ ...form, memo: e.target.value })}
            maxLength={200}
            placeholder="例: 食後に服用"
          />
        </label>

        <fieldset className="schedule-fieldset">
          <legend>服薬時間 *</legend>
          {form.schedules.map((time, index) => (
            <div className="schedule-row" key={index}>
              <input
                type="time"
                value={time}
                required
                onChange={(e) => updateSchedule(index, e.target.value)}
              />
              <button
                type="button"
                className="small-btn"
                onClick={() => removeSchedule(index)}
                disabled={form.schedules.length <= 1}
              >
                削除
              </button>
            </div>
          ))}
          <button
            type="button"
            className="button secondary"
            onClick={addSchedule}
            disabled={form.schedules.length >= MAX_SCHEDULES}
          >
            + 時間を追加
          </button>
        </fieldset>

        <label className="checkbox">
          <input
            type="checkbox"
            checked={form.active}
            onChange={(e) => setForm({ ...form, active: e.target.checked })}
          />
          通知対象にする
        </label>

        <button className="button" type="submit">
          保存
        </button>
      </form>
    </section>
  );
}

function MedicineDetail() {
  const { id } = useParams();
  const [medicine, setMedicine] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    getMedicine(id)
      .then(setMedicine)
      .catch((err) => setError(err.message));
  }, [id]);

  if (error) {
    return (
      <section className="card">
        <BackButton />
        <p className="error">{error}</p>
      </section>
    );
  }

  if (!medicine) return null;

  return (
    <section className="card">
      <BackButton />
      <h2>{medicine.name}</h2>
      <p>{medicine.memo || "メモなし"}</p>
      <p>
        服薬時間: <strong>{formatSchedules(medicine.schedules)}</strong>
      </p>
      <p>
        状態: <strong>{medicine.active ? "通知対象" : "停止中"}</strong>
      </p>
      <Link to={`/medicines/${medicine.id}/edit`} className="button">
        編集する
      </Link>
    </section>
  );
}

export default function App() {
  return (
    <main className="layout">
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/medicines" element={<MedicineList />} />
        <Route path="/medicines/new" element={<MedicineForm isEdit={false} />} />
        <Route path="/medicines/:id/edit" element={<MedicineForm isEdit />} />
        <Route path="/medicines/:id" element={<MedicineDetail />} />
      </Routes>
    </main>
  );
}
