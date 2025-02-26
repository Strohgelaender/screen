package de.fll.screen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "screen_settings")
public class ScreenSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	@Column(name = "id")
	private long id;

	@Column(name = "show_footer", nullable = false)
	private boolean showFooter;

	@Column(name = "background_image", nullable = true)
	private String backgroundImage;

	@ElementCollection
	@CollectionTable(name = "footer_images", joinColumns = @JoinColumn(name = "screen_settings_id"))
	@Column(name = "images")
	private List<String> footerImages;

	public ScreenSettings() {
	}

	public ScreenSettings(boolean showFooter, String backgroundImage, List<String> footerImages) {
		this.showFooter = showFooter;
		this.backgroundImage = backgroundImage;
		this.footerImages = footerImages;
	}

	public boolean isShowFooter() {
		return showFooter;
	}

	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public List<String> getFooterImages() {
		return footerImages;
	}

	public void setFooterImages(List<String> footerImages) {
		this.footerImages = footerImages;
	}
}
