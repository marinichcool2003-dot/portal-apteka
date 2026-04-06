import { Link } from 'react-router-dom'
import logo from '../assets/static-images/logo.png'
import question from '../assets/static-images/Вопрос.svg'
import menu from '../assets/static-images/Иконка меню.svg'
import plus from '../assets/static-images/Плюс.svg'
import lupa from '../assets/static-images/Лупа.svg'
import avatar from '../assets/static-images/Котик.png'

import '../styles/FirstNavStyle.css'

export default function FirstNav({ onToggleSidebar, onOpenTaskSearch }) {
    return (
        <nav className="firstNav">
            <div className="nav-top">
                <Link to="/dashboard"><img src={logo} alt="Логотип" className="logo" /></Link>
                <button
                    type="button"
                    className="nav-btn"
                    onClick={() => onOpenTaskSearch?.()}
                    aria-label="Поиск задач"
                >
                    <img src={lupa} alt="" className="lupa" />
                </button>
                <Link to="/createTask"><img src={plus} alt="Добавить задачу" className="plus" /></Link>
            </div>
            <div className="nav-bottom">
                <button className="nav-btn" onClick={onToggleSidebar}>
                    <img src={menu} alt="Меню" className="menu" />
                </button>
                <a href="#"><img src={question} alt="Вопросы" className="question" /></a>
                <Link to="/profile"><img src={avatar} alt="Личный кабинет" className="avatar" /></Link>
            </div>
        </nav>
    )
}