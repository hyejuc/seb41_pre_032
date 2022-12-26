import { BrowserRouter, Routes, Route } from 'react-router-dom';
import GlobalStyle from './GlobalStyle'
import Header from "./Components/Header";
import Home from './Pages/Home';
import Login from "./Pages/Login";
import QuestionCreate from './Pages/QuestionCreate';
import QuestionView from './Pages/QuestionView';
import SignUp from './Pages/SignUp';


function App() {
  return (
    <div className="App">
      <GlobalStyle />
      <BrowserRouter>
        <Header />
          <Routes>
            <Route path="/" element={<Home />} />
            {/* 로그인이 되면 위 화면을 렌더링 */}
            <Route path="/login" element={<Login />} />
            {/* 로그인이 되지 않으면 위 화면을 렌더링 */}
            <Route path="/signup" element={<SignUp />} />
            <Route path="/question/:id" element={<QuestionView />} />
            <Route path="/question/create" element={<QuestionCreate />} />
          </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
