export default function EditSection() {
    return (
        <form className="edit-form">
            <div className="edit-header__container">
                <div className="edit-header-title">
                    <p>№ 552155 <span>Не работает доступ к VPN</span></p>
                    <ol className="color-items-list">
                        <li>
                            <div className="process-item">
                                <span>{"В процессе"}</span>
                            </div>
                        </li>  
                        <li>
                            <div className="process-item">
                                <span>{"Высокий"}</span>
                            </div>
                        </li> 
                        <li>
                            <div className="process-item">
                                <span>До конца выполнения: 3 дня</span>
                            </div>
                        </li>  
                    </ol>
                </div>
            </div>
            <div className="edit-header-description">
                <ol>
                    <li>Автор: {}</li>
                </ol>
            </div>
        </form>
    )
}