import logo from '../assets/static-images/logo.png'
import question from '../assets/static-images/Вопрос.svg'
import menu from '../assets/static-images/Иконка меню.svg'
import plus from '../assets/static-images/Плюс.svg'
import lupa from '../assets/static-images/Лупа.svg'
import avatar from '../assets/static-images/Котик.png'

import '../styles/FirstNavStyle.css'

export default function FirstNav() {
    return (
        <div className="firstNav">
            <div className="nav-top">
                <a href="#"><img src={logo} alt="Логотип" className="logo" /></a>
                <a href="#"><img src={lupa} alt="Поиск" className="lupa" /></a>
                <a href="#"><img src={plus} alt="Добавить задачу" className="plus" /></a>
            </div>
            <div className="nav-bottom">
                <a href="#"><img src={menu} alt="Меню" className="menu" /></a>
                <a href="#"><img src={question} alt="Вопросы" className="question" /></a>
                <a href="#"><img src={avatar} alt="Аватарка" className="avatar" /></a>
            </div>
        </div>
    )
}