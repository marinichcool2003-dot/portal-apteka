import groupAvatar from '../assets/Аватар группы.png'
import house from '../assets/Главная 1.svg'
import tasksIcon from '../assets/Задачи 1.svg'
import notifications from '../assets/Иконка уведомления.svg'
import employee from '../assets/Сотрудники 1.svg'
import news from '../assets/Значок новостей.svg'
import settings from '../assets/Настройки 1.svg'

import '../styles/SecondNav.css'

export default function SecondNav() {
    return (
        <div className="secondNav">
            <div className="groupDescription">
                <a href="#"><img src={groupAvatar} alt="Аваттар группы" className="groupAvatar" /></a>
                <div className="groupInfo">
                    <p className="groupName">Проект: IT-отдел</p>
                    <p className="groupDescriptionText">Хз возможно какой-то текст</p>
                </div>
            </div>
            <nav>
                <ul className="icon-links">
                    <li><a href="#"><div className="icon-square"><img src={house} alt="Главная" /><span className="nav-text">Главная</span></div></a></li>
                    <li><a href="#"><div className="icon-square"><img src={tasksIcon} alt="Задачи" /><span className="nav-text">Задачи</span></div></a></li>
                    <li><a href="#"><div className="icon-square"><img src={notifications} alt="Уведомления" /><span className="nav-text">Уведомления</span></div></a></li>
                    <li><a href="#"><div className="icon-square"><img src={employee} alt="Сотрудники" /><span className="nav-text">Сотрудники</span></div></a></li>
                    <li><a href="#"><div className="icon-square"><img src={news} alt="Новости" /><span className="nav-text">Новости</span></div></a></li>
                    <li><a href="#"><div className="icon-square"><img src={settings} alt="Настройки" /><span className="nav-text">Настройки</span></div></a></li>
                </ul>
            </nav>
        </div>
    )
}