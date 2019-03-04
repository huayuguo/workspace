#include <linux/string.h>
#include <linux/wait.h>
#include <linux/platform_device.h>
#include <linux/kernel.h>
#include <linux/mm.h>
#include <linux/mm_types.h>
#include <linux/module.h>
#include <linux/types.h>
#include <linux/kthread.h>
#include <linux/gpio.h>
#include <linux/input.h>
#include <linux/interrupt.h>
//#include <mt-plat/mt_gpio.h>
//#include <mt-plat/mt_boot_common.h>

#ifdef CONFIG_OF
#include <linux/of.h>
#include <linux/of_irq.h>
#include <linux/of_address.h>
#include <linux/of_device.h>
#include <linux/of_gpio.h>
#include <linux/regulator/consumer.h>
#include <linux/clk.h>
#endif
#include <mach/mt_gpio.h>

#define LED_GPIO_PIN	(GPIO23 | 0x80000000)

static int g_status;

static void led_set_gpio_output(unsigned int gpio, unsigned int output)
{
	pr_notice("[my_led] led_set_gpio_output: %d, %d\n", gpio, output);
    //mt_set_gpio_mode(gpio, GPIO_MODE_00);
    //mt_set_gpio_dir(gpio, GPIO_DIR_OUT);
    //mt_set_gpio_out(gpio, (output>0)? GPIO_OUT_ONE: GPIO_OUT_ZERO);
}

static ssize_t show_status_value(struct device *dev, struct device_attribute *attr,
					char *buf)
{
	sprintf(buf, "%d\n", g_status);
	pr_notice("[my_led] %s\n", buf);
	return strlen(buf);
}
/*----------------------------------------------------------------------------*/
static ssize_t store_status_value(struct device *dev, struct device_attribute *attr,
					 const char *buf, size_t count)
{
	int value, c;
	char *after;

	pr_notice("[my_led] [%d] %s\n", (int)count, buf);
	value = simple_strtol(buf, &after, 0);
	c = after - buf;  
	if(c) {
		g_status = value;
		led_set_gpio_output(LED_GPIO_PIN,value);
	} else {
		pr_err("[my_led] invalid content: '%s'\n", buf);
	}
	
	return count;
}

static DEVICE_ATTR(status, S_IWUSR | S_IRUGO, show_status_value, store_status_value);

static int led_probe(struct platform_device *dev)
{
	int ret;
	ret = device_create_file(&(dev->dev), &dev_attr_status);
	if(ret) {
		pr_err("[my_led] device_create_file (%s) = %d\n", dev_attr_status.attr.name, ret);
	}
	g_status = 0;
	return ret;
}

struct platform_device led_device = {
    .name   = "my_led",
    .id     = -1,
};

static struct platform_driver led_driver = {
	.probe   = led_probe, 
	.driver = {
		.name = "my_led",
		.owner = THIS_MODULE,
	},
};			

static int __init my_led_init(void)
{
	pr_notice("[my_led] Register driver\n");

    if (platform_device_register(&led_device)) {
        return -ENODEV;
    }   
	if (platform_driver_register(&led_driver)) {
		return -ENODEV;
	}
	return 0;
}

static void __exit my_led_exit(void)
{
	platform_driver_unregister(&led_driver);
	pr_notice("[my_led] Unregister driver done\n");
}

module_init(my_led_init);
module_exit(my_led_exit);
MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("leds driver");
MODULE_AUTHOR("jovec168@sina.com");

