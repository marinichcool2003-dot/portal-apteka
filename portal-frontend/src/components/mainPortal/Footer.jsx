import logo from '../../assets/static-images/main-portal-icons/logo.svg'
import map from '../../assets/static-images/main-portal-icons/map.svg'
import vk from '../../assets/static-images/main-portal-icons/vk.svg'
import tg from '../../assets/static-images/main-portal-icons/tg.svg'
import ok from '../../assets/static-images/main-portal-icons/ok.svg'

import '../../styles/mainPortal/Footer.css'

export default function Footer() {
    return (
        <footer className="footer">
            <div className="footer-container">
                {/* <!-- Логотип и описание --> */}
                <div className="footer-section">
                    <div className="footer-logo">
                        <object type="image/svg+xml" data={logo} width="240" height="100"></object>
                    </div>
                    <p className="footer-description">
                        Данное веб-приложение разработано для сотрудников ООО "Социальная Аптека".
                        Обеспечивает более продуктивную и комфортную работу сотрудников.
                    </p>
                </div>

                {/* <!-- Быстрые ссылки --> */}
                <div className="footer-section">
                    <h3 className="footer-title">Навигация</h3>
                    <ul className="footer-links">
                        <li><a href="#Redmine">Redmine</a></li>
                        <li><a href="#Saby">СБИС</a></li>
                        <li><a href="#Suppliers">Поставщики</a></li>
                        <li><a href="#Wiki">Полезные ссылки</a></li>
                        <li><a href="#Additional">Доп. Сервисы</a></li>
                        <li><a href="#OnlineOrders">Доп. сслыки</a></li>
                    </ul>
                </div>

                {/* <!-- Контакты --> */}
                <div className="footer-section">
                    <h3 className="footer-title">Адрес</h3>
                    <div className="footer-contacts">
                        <div className="contact-item">
                            <object type="image/svg+xml" data={map} width="24" height="24"></object>
                            <span className="text-adress">г. Ростов-на-Дону, Киргизская, 14Б</span>
                        </div>
                    </div>
                </div>
                <div className="footer-section">
                    <h3 className="footer-title">Мы в соцсетях</h3>
                    <div className="social-links">
                        <a href="https://vk.com/socialapteka?ysclid=mg6bau3toc534391589" className="social-link" aria-label="ВКонтакте">
                            <object type="image/svg+xml" data={vk} width="34" height="34"></object>
                        </a>
                        <a href="https://t.me/s/socialaptekaru?ysclid=mg6bbkgymm481678337" className="social-link" aria-label="Телеграм">
                            <object type="image/svg+xml" data={tg} width="34" height="34"></object>
                        </a>
                        <a href="https://m.ok.ru/group/55183359934625" className="social-link" aria-label="Одноклассники">
                            <object type="image/svg+xml" data={ok} width="34" height="34"></object>
                        </a>
                    </div>
                    {/* <!-- Дополнительные ссылки --> */}
                    <div className="footer-additional">
                        <a href="#" id="remoteHelpLink" className="footer-btn">Удаленная помощь</a>
                        <a href="#">Карта аптек</a>
                    </div>
                </div>
            </div>
            {/* <!-- Копирайт --> */}
            <div className="footer-bottom">
                <div className="footer-container">
                    <p>&copy; 2025 Социальная аптека. Все права защищены.</p>
                </div>
            </div>
            <div id="passwordModal" className="modal">
                <div className="modal-content">
                    <span className="close">&times;</span>
                    <h3>Доступ к программам удаленного доступа</h3>
                    <p>Введите пароль для скачивания:</p>
                    <input type="password" id="passwordInput" placeholder="Введите пароль"/>
                        <button id="submitPassword">Скачать архив</button>
                        <p id="errorMessage" className="error-message"></p>
                </div>
            </div>
        </footer>
    )
}